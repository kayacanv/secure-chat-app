var express = require('express');
var bodyParser = require('body-parser');
const mongoose = require('mongoose');
var Pusher = require('pusher');

var app = express();

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));

const pusher = new Pusher({
  appId: "",
  key: "",
  secret: "",
  cluster: "",
  useTLS: true
});

mongoose.connect('mongodb://127.0.0.1/db', { useNewUrlParser: true });

const Schema = mongoose.Schema;
const userSchema = new Schema({
    name: { type: String, required: true, },
    publicKey: {type: String,  required: true,}
  });
var User = mongoose.model('User', userSchema);


// make this available to our users in our Node applications
module.exports = User;
var currentUser;


app.post('/login', (req, res) => {
    const myModel = mongoose.model('User');
    myModel.findOne({ name: req.body.name }, function (err, user) {
        if (err) {
            res.send("Error connecting to database");
        }
        if (user) {
            // user exists already
            currentUser = user;
            res.status(200).send(user)
        } else {
            // create new user
            var newuser = new User({
                name: req.body.name,
                publicKey: req.body.publicKey
              });

              newuser.save(function(err) {
                if (err){ 
                  res.status(400).send(err)
                  console.log('LOGIN ERROR!: ', err);
                }
                else {
                  console.log('User saved successfully!: ', newuser);
                  currentUser = newuser;
                  res.status(200).send(newuser)
                }
            })

        }
    });

})


// fetch all users
app.get('/users', (req, res) => {
    User.find({}, function(err, users) {
        if (err) throw err;
        // object of all the users
        res.send(users);
      });
})

// authenticate users for the presence channel
app.post('/pusher/auth/presence', (req, res) => {
    var socketId = req.body.socket_id;
    var channel = req.body.channel_name;
    var presenceData = {
      user_id: currentUser._id,
      user_info: {publicKey: currentUser.publicKey, name: currentUser.name}
    };
    console.log('presence: ', presenceData)
    res.send(pusher.authenticate(socketId, channel, presenceData));
});

// authenticate users for the private channel
app.post('/pusher/auth/private', (req, res) => {
  console.log('auth private: ', req.body);
  res.send(pusher.authenticate(req.body.socket_id, req.body.channel_name));
});

app.post('/send-message', (req, res) => {
    console.log('send-message: ', req.body);
    pusher.trigger(req.body.channel_name, 'new-message', {message: req.body.message, sender_id: req.body.sender_id});
    res.sendStatus(200);
});

var port = process.env.PORT || 5000;
app.listen(port);
