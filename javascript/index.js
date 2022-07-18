const Jwt = require("./jwt");
const fs = require('fs');
const privateKey = fs.readFileSync(`key.pem`).toString('utf8');

const main = () => {
    const jwt = new Jwt({
        iss: "iss",
        sub: "sub",
        aud: "aud",
        privateKey,
    });

    const token = jwt.token;
    console.log({ token });
}

main();