const Client = require('./client');
const templateKey = 'com.mesilat.blueprint-validation-demo:bvp-company-template';

module.exports = async (options) => {
  const client = new Client(options);

  await client.validatePage('5439503', templateKey);
};
