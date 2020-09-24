import React, { Component } from "react";

class Navbar extends Component {
  render() {
    return (
      <nav class="navbar navbar-light bg-light">
        <a class="navbar-brand" href="#">
          <img
            src="https://www.clipartmax.com/png/full/54-545682_doctor-logo-doctor-logo-png.png"
            width="30"
            height="30"
            class="d-inline-block align-top mr-2"
            alt=""
          />
          HealthAI
        </a>
        {this.props.isSignedIn && (
          <button
            className="btn btn-danger float-right"
            onClick={() => this.props.onSignoutClicked()}
          >
            Logout
          </button>
        )}
      </nav>
    );
  }
}

export default Navbar;
