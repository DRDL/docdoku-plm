var DocdokuPlmApi = require('../../target/docdoku-plm-api/npm');
var basePath = 'http://localhost:8080/api';

var guestClient = new DocdokuPlmApi.ApiClient();
guestClient.basePath = basePath;

var langApi = new DocdokuPlmApi.LanguagesApi(guestClient);

langApi.getLanguages(function (err, languages, response) {
    if (err) {
        console.log('Error while fetching languages ' + response.status);
        return null;
    }
    console.log('Languages : ' + languages);
});

var authApi = new DocdokuPlmApi.AuthApi(guestClient);

authApi.login(DocdokuPlmApi.LoginRequestDTO.constructFromObject({
    login: 'test',
    password: 'test'
}), function (err, account, response) {
    if (err) {
        console.log('Error while login ' + response.status);
        return null;
    }
    console.log('Login successful');
    console.log(account);
    console.log('Token');
    console.log(response.headers.jwt);



});

