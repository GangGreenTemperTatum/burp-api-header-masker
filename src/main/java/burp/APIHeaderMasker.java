package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import java.util.ArrayList;
import java.util.List;

public class APIHeaderMasker implements BurpExtension {
    private static final String MASKED_VALUE = "**burp-api-header-masker**";

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("API Header Masker");

        // Register HTTP handler
        api.http().registerHttpHandler(new HttpHandler() {
            @Override
            public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
                // Let the original request go through unmodified
                return RequestToBeSentAction.continueWith(requestToBeSent);
            }

            @Override
            public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
                HttpResponse response = responseReceived.response();
                List<HttpHeader> headers = response.headers();
                boolean needsModification = false;

                // Create new list for modified headers
                List<HttpHeader> maskedHeaders = new ArrayList<>();

                // Check each header
                for (HttpHeader header : headers) {
                    if (header.name().toLowerCase().contains("api")) {
                        maskedHeaders.add(HttpHeader.httpHeader(header.name(), MASKED_VALUE));
                        needsModification = true;
                    } else {
                        maskedHeaders.add(header);
                    }
                }

                // If we found headers to mask, create new response with masked headers
                if (needsModification) {
                    HttpResponse maskedResponse = response.withHeaders(maskedHeaders);
                    return ResponseReceivedAction.continueWith(maskedResponse);
                }

                return ResponseReceivedAction.continueWith(response);
            }
        });

        // Register HTTP response editor
        api.userInterface().registerHttpResponseEditor(new HttpResponseEditor() {
            @Override
            public HttpResponse getResponse() {
                return null;
            }

            @Override
            public boolean isEnabledFor(HttpRequestResponse requestResponse) {
                // Enable for all responses
                return true;
            }

            @Override
            public void setRequestResponse(HttpRequestResponse requestResponse) {
                if (requestResponse != null && requestResponse.response() != null) {
                    HttpResponse response = requestResponse.response();
                    List<HttpHeader> headers = response.headers();
                    boolean needsModification = false;

                    // Create new list for modified headers
                    List<HttpHeader> maskedHeaders = new ArrayList<>();

                    // Check each header
                    for (HttpHeader header : headers) {
                        if (header.name().toLowerCase().contains("api")) {
                            maskedHeaders.add(HttpHeader.httpHeader(header.name(), MASKED_VALUE));
                            needsModification = true;
                        } else {
                            maskedHeaders.add(header);
                        }
                    }

                    // If we found headers to mask, create new response with masked headers
                    if (needsModification) {
                        requestResponse = requestResponse.withResponse(
                            response.withHeaders(maskedHeaders)
                        );
                    }
                }
            }

            @Override
            public boolean isModified() {
                return false;
            }

            @Override
            public byte[] getResponseBytes() {
                return new byte[0];
            }
        });
    }
}
