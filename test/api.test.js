const fs = require('fs');
const { join } = require('path');
const options = require('./.test.settings.js');
const createPages = require('./components/create-pages');

options.space = 'Demo';
options.spaceKey = 'DEMO';
options.spaceRootPageId = 327683;

describe('Validating Blueprints Demo API tests', () => {
  beforeEach(async () => {
    jest.setTimeout(1800000);
  });

  it('Create a demo page set', async () => await createPages(options));
});
