import React, { Component } from "react";
import "./App.css";
import Navbar from "./components/navbar";
import Body from "./components/body";
import firebase from "firebase";
import StyledFirebaseAuth from "react-firebaseui/StyledFirebaseAuth";

firebase.initializeApp({
  apiKey: "AIzaSyCMssWHvsze0yxdJ6UDGzaeRm2KWU0s-ys",
  authDomain: "healthcareai-cmpe295.firebaseapp.com"
});

class App extends Component {
  state = {
    isSignedIn: false
  };
  uiConfig = {
    signInFlow: "popup",
    signInOptions: [
      firebase.auth.GoogleAuthProvider.PROVIDER_ID,
      firebase.auth.EmailAuthProvider.PROVIDER_ID
    ],
    callbacks: {
      signInSuccess: () => false
    }
  };

  componentDidMount = () => {
    firebase.auth().onAuthStateChanged(user => {
      this.setState({ isSignedIn: !!user });
      console.log("user", user);
    });
  };

  handleSingoutClicked = () => {
    firebase.auth().signOut();
  };

  render() {
    return (
      <div className="App">
        {/* {this.state.isSignedIn ? (
          <React.Fragment>
            <p>Signed in</p>
            <p>welcome {firebase.auth().currentUser.displayName}</p>
            <img alt="pic" src={firebase.auth().currentUser.photoURL} />
            <br />
            <button
              className="btn btn-primary"
              onClick={() => firebase.auth().signOut()}
            >
              Signout
            </button>
          </React.Fragment>
        ) : (
          <StyledFirebaseAuth
            uiConfig={this.uiConfig}
            firebaseAuth={firebase.auth()}
          />
        )} */}
        <Navbar
          isSignedIn={this.state.isSignedIn}
          onSignoutClicked={this.handleSingoutClicked}
        />
        {this.state.isSignedIn ? (
          <Body currentUser={firebase.auth().currentUser} />
        ) : (
          <StyledFirebaseAuth
            className="mt-5"
            uiConfig={this.uiConfig}
            firebaseAuth={firebase.auth()}
          />
        )}
      </div>
    );
  }
}
export default App;
