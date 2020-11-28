import React, { Component } from "react";
import { db } from "../conf/firebase_db.js";
import firebase from "firebase";
import { NavLink } from "react-router-dom";
import "../styling/profile.css";

import { Route, Switch } from "react-router-dom";

class UserProfile extends Component {
  constructor(props) {
    super(props)
    this.state = {
      name: "",
      age: "",
      address: "",
      height: "",
      weight: "",
      doctor_number: "",
      fall_last_test_time: "",
      respiratory_last_test_time: new Date(),
      fall_last_test_result: "",
      respiratory_last_test_result: "",

    }
  }
  async componentWillMount() {
    const profile_collection = db.collection("profile")
    //Get data from Fall Documents
    var profile = await profile_collection.where("email", "==", firebase.auth().currentUser.email).get();
    const profile_data = profile.docs.map(doc => doc.data());
    var name = firebase.auth().currentUser.displayName;
    console.log("Name:", profile_data[0]);
    this.setState(
      {
        user_name: name,
        user_age: profile_data[0].age,
        height: profile_data[0].height,
        weight: profile_data[0].weight,
        doctor_contact: profile_data[0].docNo,
        address: profile_data[0].address

      }
    );
  }


  render() {
    // console.log("in body", this.props.currentUser);
    const component_style = {
      width: "150px",
      height: "20px",
      paddingTop: "15px",
      paddingRight: "15px",
      paddingBottom: "15px",
      textAlign: "center",
      paddingLeft: "20px",
      marginTop: "25px"

    };
    const { photoURL, displayName, email } = firebase.auth().currentUser;
    
    return (
      <div >
      
        <br/>
        <div class="row">
        <div class="profile_card col-md-4" >
         
          
            <br/>
            <h5>{this.state.user_name}</h5>
            <p class="profile_title"> Age: {this.state.user_age}</p>
            <p class="profile_title"> Height: {this.state.height}</p>
            <p class="profile_title"> weight: {this.state.weight}</p>
            <p class="profile_title"> Primary Doctor Contact: {this.state.doctor_contact}</p>
            <p class="profile_title"> Address: {this.state.address}</p>
       

            
            <br />
        </div>
        </div>
        </div>
    );
  }
}

export default UserProfile;
