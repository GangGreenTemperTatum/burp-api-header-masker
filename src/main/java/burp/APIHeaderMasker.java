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
                List<HttpHeader> headers = responseReceived.headers();
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
                    return ResponseReceivedAction.continueWith(
                        responseReceived.withBody(responseReceived.body())
                                      .withStatusCode(responseReceived.statusCode())
                                      .withHeaders(maskedHeaders)
                    );
                }

                return ResponseReceivedAction.continueWith(responseReceived);
            }
        });

        // Register HTTP response handler for Proxy
        api.proxy().registerResponseHandler(new ProxyResponseHandler() {
            @Override
            public ProxyResponseReceivedAction handleResponseReceived(InterceptedResponse interceptedResponse) {
                List<HttpHeader> headers = interceptedResponse.headers();
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
                    return ProxyResponseReceivedAction.continueWith(
                        interceptedResponse.withBody(interceptedResponse.body())
                                         .withStatusCode(interceptedResponse.statusCode())
                                         .withHeaders(maskedHeaders)
                    );
                }

                return ProxyResponseReceivedAction.continueWith(interceptedResponse);
            }

            @Override
            public ProxyResponseToBeSentAction handleResponseToBeSent(InterceptedResponse interceptedResponse) {
                return ProxyResponseToBeSentAction.continueWith(interceptedResponse);
            }
        });
    }
}