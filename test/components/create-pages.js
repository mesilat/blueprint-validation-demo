const { readFileSync } = require('fs');
const { join } = require('path');
const soynode = require('soynode');
const Client = require('./client');
const templateKey = 'com.mesilat.blueprint-validation-demo:bvp-company-template';

async function compileTemplates() {
  return new Promise((resolve, reject) => {
    soynode.compileTemplates(join(__dirname, '..', 'templates'), async function (err){
      if (err) {
        reject(err);
      } else {
        resolve(soynode);
      }
    });
  });
}

module.exports = async (options) => {
  const companies = JSON.parse(readFileSync(join(__dirname, '..', 'data', 'companies.json'), 'utf8'));
  const soynode = await compileTemplates();
  const client = new Client(options);

  for (let i = 0; i < companies.length; i++) {
    const company = companies[i];
    const title = company.title;
    const body = soynode.render('Templates.company', { data: company });
    const page = await client.createPage(options.spaceKey, options.spaceRootPageId, title, body);
    const pageId = page.id;
    await client.postLabel(pageId, { prefix: 'global', name: 'company-demo' });
    await client.validatePage(pageId, templateKey);
    console.debug(`Created page ${title}`);
  }
};
