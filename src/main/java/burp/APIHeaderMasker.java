package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.*;
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
                HttpResponse originalResponse = responseReceived.responseResponse();
                List<HttpHeader> headers = originalResponse.headers();
                boolean needsModification = false;
                List<HttpHeader> maskedHeaders = new ArrayList<>();

                for (HttpHeader header : headers) {
                    if (header.name().toLowerCase().contains("api")) {
                        maskedHeaders.add(HttpHeader.httpHeader(header.name(), MASKED_VALUE));
                        needsModification = true;
                    } else {
                        maskedHeaders.add(header);
                    }
                }

                if (needsModification) {
                    HttpResponse maskedResponse = HttpResponse.httpResponse()
                        .withBody(originalResponse.body())
                        .withHeaders(maskedHeaders)
                        .withStatusCode(originalResponse.statusCode())
                        .build();
                    return ResponseReceivedAction.continueWith(maskedResponse);
                }

                return ResponseReceivedAction.continueWith(originalResponse);
            }
        });

        // Register HTTP response handler for Proxy
        api.proxy().registerResponseHandler(new ProxyResponseHandler() {
            @Override
            public ProxyResponseReceivedAction handleResponseReceived(InterceptedResponse interceptedResponse) {
                HttpResponse originalResponse = interceptedResponse.responseResponse();
                List<HttpHeader> headers = originalResponse.headers();
                boolean needsModification = false;
                List<HttpHeader> maskedHeaders = new ArrayList<>();

                for (HttpHeader header : headers) {
                    if (header.name().toLowerCase().contains("api")) {
                        maskedHeaders.add(HttpHeader.httpHeader(header.name(), MASKED_VALUE));
                        needsModification = true;
                    } else {
                        maskedHeaders.add(header);
                    }
                }

                if (needsModification) {
                    HttpResponse maskedResponse = HttpResponse.httpResponse()
                        .withBody(originalResponse.body())
                        .withHeaders(maskedHeaders)
                        .withStatusCode(originalResponse.statusCode())
                        .build();
                    return ProxyResponseReceivedAction.continueWith(maskedResponse);
                }

                return ProxyResponseReceivedAction.continueWith(originalResponse);
            }

            @Override
            public ProxyResponseToBeSentAction handleResponseToBeSent(InterceptedResponse interceptedResponse) {
                return ProxyResponseToBeSentAction.continueWith(interceptedResponse);
            }
        });
    }
}