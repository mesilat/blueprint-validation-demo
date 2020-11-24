const ConfluenceClient = require('confluence-client');
const REST_API_PATH = "/rest/blueprint-validation/1.0";

function Client(options) {
    ConfluenceClient.apply(this, [options]);
}
Client.prototype = Object.create(ConfluenceClient.prototype);
Client.prototype.constructor = Client;
Client.prototype.validatePage = async function(pageId, templateKey) {
  if (templateKey) {
    return this.post(`${REST_API_PATH}/data/validate/${pageId}?templateKey=${encodeURIComponent(templateKey)}`);
  } else {
    return this.post(`${REST_API_PATH}/data/validate/${pageId}`);
  }
}

module.exports = Client;
