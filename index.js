var mongodb = require('mongodb');
var ObjectID = mongodb.ObjectID;
var crypto = require('crypto');
var express = require('express');
var bodyParser = require('body-parser');

//Password utils
//create function to random salt
var genRandomString = function(length){
    return crypto.randomBytes(Math.ceil(length/2))
        .toString('hex')
        .slice(0,length);
};

var sha512 = function(password, salt){
    var hash = crypto.createHmac('sha512', salt);
    hash.update(password);
    var value = hash.digest('hex');
    return{
        salt:salt,
        passwordHash:value
    };
};

function saltHashPassword(userPassword){
    var salt = genRandomString(16);
    var passwordData = sha512(userPassword, salt);
    return passwordData;
}

function checkHashPassword(userPassword,salt){
    var passwordData = sha512(userPassword, salt);
    return passwordData;
}

//create express service
var app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

//create mongoDB Client
var MongoClient = mongodb.MongoClient;

//create URL
var url = 'mongodb://localhost:27017'

MongoClient.connect(url, {useNewUrlParser: true}, function(err,client) {
    if (err)
        console.log('Unable to connect to the mongoDB server.Error', err);
    else {
        //register
        app.post('/register', (request, response, next)=>{
            var post_data = request.body;

            var plaint_password = post_data.password;
            var hash_data = saltHashPassword(plaint_password);

            var password = hash_data.passwordHash; //save password hash
            var salt = hash_data.salt; //save salt
            var name = post_data.name;
            var email = post_data.email;

            var insertJson = {
                'email' : email,
                'password' : password,
                'salt' : salt,
                'name' : name
            };
            var db = client.db('edmtdevnodejs');

            // chedck exists email
            db.collection('user')
                .find({'email':email}).count(function(err,number){
                    if (number != 0)
                    {
                        response.json('Email already exists');
                        console.log('Email already exsits');
                    }
                    else
                    {
                        //insert data
                        db.collection('user')
                            .insertOne(insertJson, function(error, res){
                                response.json('Registration sucess');
                                console.log('Registration sucess');
                            })
                    }
            })
        });

        app.post('/login', (request, response, next)=>{
            var post_data = request.body;

            var email = post_data.email;
            var userPassword = post_data.password;

            var db = client.db('edmtdevnodejs');

            // chedck exists email
            db.collection('user')
                .find({'email':email}).count(function(err,number){
                if (number == 0)
                {
                    response.json('Email not exists');
                    console.log('Email not exsits');
                }
                else
                {
                    //insert data
                    db.collection('user')
                        .findOne({'email':email}, function(err, user){
                            var salt = user.salt;
                            var hashed_password = checkHashPassword(userPassword,salt).passwordHash;
                            var encrypted_password = user.password;
                            if (hashed_password == encrypted_password)
                            {
                                response.json('Login success');
                                console.log('Login success');
                            }
                            else {
                                response.json('Wrong password');
                                console.log('Wrong password');
                            }
                        })
                }
            })
        });

        //Start the web server
        app.listen(3000, () => {
            console.log('Connected to MongoDB Server, WebService running on port 3000');
        })
    }
})
