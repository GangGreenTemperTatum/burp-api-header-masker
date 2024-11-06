from burp import IBurpExtender, IHttpListener, IMessageEditorTabFactory, IMessageEditorTab, IProxyListener, ITab, IMessageEditorController
from java.io import PrintWriter
from java.awt import Component
from javax.swing import JTable
from javax.swing.table import TableColumn
from java.util import ArrayList

class BurpExtender(IBurpExtender, IHttpListener, IMessageEditorTabFactory, IProxyListener, IMessageEditorController):
    def registerExtenderCallbacks(self, callbacks):
        # Initialize extension
        self._callbacks = callbacks
        self._helpers = callbacks.getHelpers()

        # Set extension name
        callbacks.setExtensionName("API Header Masker")

        # Store original responses and log
        self._originalResponses = {}
        self._log = ArrayList()
        self._currentlyDisplayedItem = None

        # Register ourselves as various listeners
        callbacks.registerHttpListener(self)
        callbacks.registerMessageEditorTabFactory(self)
        callbacks.registerProxyListener(self)

        # Customize Proxy History Table
        proxyHistory = callbacks.getProxyHistory()
        if isinstance(proxyHistory, JTable):
            tableModel = proxyHistory.getModel()
            column = TableColumn(tableModel.getColumnCount())
            column.setHeaderValue("Original Values")
            proxyHistory.addColumn(column)

        # Get stdout for debugging
        self._stdout = PrintWriter(callbacks.getStdout(), True)
        self._stdout.println("API Header Masker loaded successfully!\n\nmade with <3 by @GangGreenTemperTatum\nhttps://github.com/GangGreenTemperTatum/burp-api-header-masker")

    def processProxyMessage(self, messageIsRequest, message):
        if not messageIsRequest:
            response = message.getMessageInfo().getResponse()
            if response:
                self._originalResponses[message.getMessageInfo()] = response
                self._log.add(LogEntry(message.getMessageInfo(), self._helpers.analyzeResponse(response)))

    def processHttpMessage(self, toolFlag, messageIsRequest, messageInfo):
        if not messageIsRequest and toolFlag == self._callbacks.TOOL_PROXY:
            response = messageInfo.getResponse()
            analyzedResponse = self._helpers.analyzeResponse(response)

            # Get response headers and body
            headers = analyzedResponse.getHeaders()
            body = response[analyzedResponse.getBodyOffset():]
            body_string = self._helpers.bytesToString(body)

            # Keywords to mask
            sensitive_words = ['token', 'secret', 'api', 'ey']
            modified = False

            new_headers = []
            for header in headers:
                header_lower = header.lower()
                if any(word in header_lower for word in sensitive_words):
                    # Split header into name and value
                    header_parts = header.split(': ', 1)
                    if len(header_parts) == 2:
                        header_name, header_value = header_parts
                        # Mask the entire value
                        new_headers.append("%s: **burp-api-header-masker**" % header_name)
                        modified = True
                    else:
                        new_headers.append(header)
                else:
                    new_headers.append(header)

            # Mask JWT tokens in body
            import re
            jwt_pattern = r'(eyJ[a-zA-Z0-9_-]*\.eyJ[a-zA-Z0-9_-]*\.[a-zA-Z0-9_-]*)'
            if re.search(jwt_pattern, body_string):
                body_string = re.sub(jwt_pattern, '**burp-api-header-masker**', body_string)
                modified = True

            # If any modifications were made, update the response
            if modified:
                new_response = self._helpers.buildHttpMessage(new_headers, self._helpers.stringToBytes(body_string))
                messageInfo.setResponse(new_response)
                # Store original response
                self._originalResponses[messageInfo] = response

    def createNewInstance(self, controller, editable):
        return OriginalResponseTab(self, controller, editable)

    # IMessageEditorController methods
    def getHttpService(self):
        return self._currentlyDisplayedItem.getHttpService() if self._currentlyDisplayedItem else None

    def getRequest(self):
        return self._currentlyDisplayedItem.getRequest() if self._currentlyDisplayedItem else None

    def getResponse(self):
        return self._currentlyDisplayedItem.getResponse() if self._currentlyDisplayedItem else None

class OriginalResponseTab(IMessageEditorTab):
    def __init__(self, extender, controller, editable):
        self._extender = extender
        self._editable = editable
        self._txtInput = extender._callbacks.createTextEditor()
        self._currentMessage = None

    def getTabCaption(self):
        return "Original Response"

    def getUiComponent(self):
        return self._txtInput.getComponent()

    def isEnabled(self, content, isRequest):
        if isRequest:
            return False
        return content in self._extender._originalResponses.values()

    def setMessage(self, content, isRequest):
        if content is None:
            self._txtInput.setText(None)
            self._currentMessage = None
        else:
            # Find and display original response
            for message, original in self._extender._originalResponses.items():
                if message.getResponse() == content:
                    self._txtInput.setText(original)
                    self._currentMessage = content
                    break

    def getMessage(self):
        return self._currentMessage

    def isModified(self):
        return False

    def getSelectedData(self):
        return self._txtInput.getSelectedText()

class LogEntry:
    def __init__(self, messageInfo, requestResponse):
        self._messageInfo = messageInfo
        self._requestResponse = requestResponse
        self._originalValue = None

    def getOriginalValue(self):
        return self._originalValue

    def setOriginalValue(self, value):
        self._originalValue = value