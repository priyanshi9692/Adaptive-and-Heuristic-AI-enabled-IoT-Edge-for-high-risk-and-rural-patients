import React, { Component } from "react";
import { NavLink } from "react-router-dom";

import { Route, Switch } from "react-router-dom";
import Dashboard from "./dashboard";
import UserProfile from "./profile";
import TestResults from "./test_results";
import firebase from "firebase";

class Body extends Component {
  render() {
    console.log("in body", this.props.currentUser);
    const { photoURL, displayName, email } = this.props.currentUser;
    return (
      <div className="row">
        <div className="col-2">
          <div class="card m-2" style={{ width: "15rem" }}>
            <img class="card-img-top " src={photoURL} alt="Card image cap" />
            <div class="card-body">
              <h5 class="card-title">{displayName}</h5>
              <p class="card-text">{email}</p>
            </div>
          </div>
        </div>
        <div className="col-10">
          <ul className="nav  nav-pills m-3">
            <li className="nav-item">
              <NavLink exact className="nav-link" to="/">
                Dashboard
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink className="nav-link" to="/profile">
                Profile
              </NavLink>
            </li>
          </ul>
          <Switch>
            <div className="card m-2">
              <Route path="/" component={Dashboard} exact />
              <Route path="/profile" component={UserProfile} exact />
              <Route path="/result" component={TestResults} exact />
            </div>
          </Switch>
        </div>
      </div>
    );
  }
}

export default Body;
