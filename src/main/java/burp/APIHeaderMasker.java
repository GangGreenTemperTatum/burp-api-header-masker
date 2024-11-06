package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.proxy.http.InterceptedResponse;
import burp.api.montoya.proxy.http.ProxyResponseHandler;
import burp.api.montoya.proxy.http.ProxyResponseReceivedAction;
import burp.api.montoya.proxy.http.ProxyResponseToBeSentAction;

import java.util.ArrayList;
import java.util.List;

public class APIHeaderMasker implements BurpExtension {
    private static final String MASKED_VALUE = "**burp-api-header-masker**";
    private static final String[] SENSITIVE_KEYWORDS = {"api", "token", "secret"};

    private boolean isHeaderSensitive(String headerName) {
        headerName = headerName.toLowerCase();
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (headerName.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private HttpResponse maskSensitiveHeaders(HttpResponse originalResponse) {
        List<HttpHeader> headers = originalResponse.headers();
        boolean needsModification = false;
        List<HttpHeader> maskedHeaders = new ArrayList<>();

        for (HttpHeader header : headers) {
            if (isHeaderSensitive(header.name())) {
                maskedHeaders.add(HttpHeader.httpHeader(header.name(), MASKED_VALUE));
                needsModification = true;
            } else {
                maskedHeaders.add(header);
            }
        }

        if (needsModification) {
            return api.http().responseBuilder()
                    .headers(maskedHeaders)
                    .body(originalResponse.body())
                    .statusCode(originalResponse.statusCode())
                    .build();
        }

        return originalResponse;
    }

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("API Header Masker");

        // Register HTTP handler for Proxy History
        api.http().registerHttpHandler(new HttpHandler() {
            @Override
            public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
                return RequestToBeSentAction.continueWith(requestToBeSent);
            }

            @Override
            public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
                HttpResponse maskedResponse = maskSensitiveHeaders(responseReceived.getResponse());
                return ResponseReceivedAction.continueWith(maskedResponse);
            }
        });

        // Register HTTP response handler for Proxy
        api.proxy().registerResponseHandler(new ProxyResponseHandler() {
            @Override
            public ProxyResponseReceivedAction handleResponseReceived(InterceptedResponse interceptedResponse) {
                HttpResponse maskedResponse = maskSensitiveHeaders(interceptedResponse.getResponse());
                return ProxyResponseReceivedAction.continueWith(maskedResponse);
            }

            @Override
            public ProxyResponseToBeSentAction handleResponseToBeSent(InterceptedResponse interceptedResponse) {
                return ProxyResponseToBeSentAction.continueWith(interceptedResponse);
            }
        });
    }
}