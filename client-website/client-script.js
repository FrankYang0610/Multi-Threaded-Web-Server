document.addEventListener('DOMContentLoaded', () => {
    const $ = id => document.getElementById(id);
    const sendGetRequestButton = $('sendGetRequestButton');
    const sendHeadRequestButton = $('sendHeadRequestButton');
    const sendWrongRequestButton = $('sendWrongRequestButton');
    const serverIPField = $('serverIP');
    const serverPortField = $('serverPort');
    const filePathField = $('filePath');
    const responseDiv = $('response');
    const enableIMS = $('enableIfModifiedSince');
    const ifModifiedSinceDateField = $('ifModifiedSinceDate');

    ifModifiedSinceDateField.value = new Date().toISOString().slice(0, 16);

    enableIMS.addEventListener('change', () => {
        ifModifiedSinceDateField.disabled = !enableIMS.checked;
    });

    // Get the parameters required for the request from the HTML form
    function getInputs() {
        const serverIP = serverIPField.value.trim();
        const serverPort = serverPortField.value.trim();
        let filePath = filePathField.value.trim();
        const connection = document.querySelector('input[name="connectionType"]:checked').value;
        const ifModifiedSince = enableIMS.checked;
        const ifModifiedSinceDate = ifModifiedSince ? new Date(ifModifiedSinceDateField.value) : null;

        if (!serverIP || !serverPort || !filePath) {
            responseDiv.innerHTML = '<p class="error">Please fill in all the fields.</p>';
            return null;
        }
        if (!filePath.startsWith('/')) filePath = '/' + filePath;

        return {
            url: `http://${serverIP}:${serverPort}${filePath}`,
            connection,
            ifModifiedSinceDate: ifModifiedSinceDate
        };
    }

    // Send the request and handle the response
    function sendRequest(method) {
        const inputs = getInputs();
        if (!inputs) return;
        console.log('Selected connection type: ', inputs.connection);
        console.log('URL: ', inputs.url)

        responseDiv.innerHTML = `<p>Sending ${method} request to ${inputs.url}...</p>`;

        // The headers of the request
        const headers = { 'Connection': inputs.connection };

        if (inputs.ifModifiedSinceDate) {
            headers['If-Modified-Since'] = inputs.ifModifiedSinceDate.toUTCString();
        }
        console.log(headers['If-Modified-Since'])

        fetch(inputs.url, {
            method,
            headers,
            cache: 'no-store'
        })
            .then(async response => {
                let output = `HTTP/1.1 ${response.status} ${response.statusText}\n`;

                for (const [key, value] of response.headers) {
                    output += `${key}: ${value}\r\n`;
                }

                if (method === 'GET') {
                    const contentType = response.headers.get('content-type') || '';
                    if (contentType.startsWith('image/')) {
                        const blob = await response.blob();
                        const imageURL = URL.createObjectURL(blob);
                        output = `<pre>${output}</pre><img src="${imageURL}" alt="Retrieved image" style="max-width: 800px; max-height: 600px;">`;
                    } else {
                        const text = await response.text();
                        output = `<pre>${output}\r\n${text}</pre>`;
                    }
                } else { // === 'HEAD'
                    output = `<pre>${output}</pre>`;
                }
                responseDiv.innerHTML = output;
            })
            .catch(err => {
                responseDiv.innerHTML = `<p class="error">Error: ${err.message}</p>`;
            });
    }

    // Add button listeners
    sendGetRequestButton.addEventListener('click', () => sendRequest('GET'));
    sendHeadRequestButton.addEventListener('click', () => sendRequest('HEAD'));
    sendWrongRequestButton.addEventListener('click', () => {
        alert(
            "Due to browser restrictions, you cannot send an invalid HTTP method to the Server. " +
            "If you want to trigger a HTTP 400 Bad Request error, " +
            "please use the Clients Simulator in the Java program."
        )
    });
});
