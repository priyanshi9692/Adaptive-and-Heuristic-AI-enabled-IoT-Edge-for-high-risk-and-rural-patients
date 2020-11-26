import React, { Component } from "react";
import { db } from "../conf/firebase_db.js";
import firebase from "firebase";
import { Pie, Doughnut } from 'react-chartjs-2';
import { NavLink } from "react-router-dom";

class Dashboard extends Component {
  constructor(props) {
    super(props)
    this.state = {
      fall_tests: 0,
      respiratory_tests: 0,
      fall_last_test_time: "",
      respiratory_last_test_time: new Date(),
      fall_last_test_result: "",
      respiratory_last_test_result: "",

    }
  }


  async componentWillMount() {
    const fall_collection = db.collection("fall")
    //Get data from Fall Documents
    var fall_data = await fall_collection.where("email", "==", firebase.auth().currentUser.email).get();
    const data = fall_data.docs.map(doc => doc.data());
    console.log(data.length); // array of cities objects
    console.log(data);
    data.sort(function (x, y) {
      return x.time.seconds - y.time.seconds;
    })
    var count = 0;

    for (var i = 0; i < data.length; i++) {
      console.log(new Date(data[i].time.seconds * 1000))
      if (data[i].result == "FALL") {
        count++;
      }
    }
    var d = new Date(data[data.length - 1].time.seconds * 1000);
    var fall_result = ""
    fall_result = data[data.length - 1].result == "FALL" ? "Fall" : "No Fall";
    this.setState(
      {
        fall_tests: data.length,
        fall_last_test_time: d.toLocaleString(),
        fall_last_test_result: fall_result
      }
    );
    // firebase.auth().currentUser.email
    //Get Latest Doc from array according to timestamp
    const respiratory_collection = db.collection("respiratory")
    var respiratory_data = await respiratory_collection.where("email", "==", firebase.auth().currentUser.email).get();
    //Get size of the array (No. of tests taken)

    const resp_data = respiratory_data.docs.map(doc => doc.data());
    console.log(resp_data.length); // array of cities objects
    console.log(resp_data[0]);
    resp_data.sort(function (x, y) {
      return x.time.seconds - y.time.seconds;
    })
    var d = new Date(resp_data[resp_data.length - 1].time.seconds * 1000);
    var resp_result = ""
    resp_result = resp_data[resp_data.length - 1].result == "No abnormalities were detected" ? "Healthy" : "Unhealthy";
    var countNormal = 0;
    var countWheezes = 0;
    var countCrackles = 0;
    var countBoth = 0;
    for (var i = 0; i < resp_data.length; i++) {
      if (resp_data[i].result == "No abnormalities were detected") {
        countNormal++;

      }
      if (resp_data[i].result == "Contains crackles") {
        countCrackles++;

      }
      if (resp_data[i].result == "Contains wheeze") {
        countWheezes++;

      }
      if (resp_data[i].result == "Contains both crackle and wheeze") {
        countBoth++;

      }
    }
    console.log(countNormal);
    console.log(countBoth);
    console.log(countWheezes);
    console.log(countCrackles);


    this.setState(
      {
        resp_tests: resp_data.length,
        resp_last_test_time: d.toLocaleString(),
        resp_last_test_result: resp_result
      }

    );

    //Get data from Respiratory Documents


    //Get Latest Doc from array according to timestamp

    //Get size of the array (No. of tests taken)


    //
    const stateFall = {
      labels: ['Fall', 'No-Fall'],
      datasets: [
        {
          label: 'Fall Test Report',
          backgroundColor: [
            '#FFC154',
            '#47B39C'

          ],
          hoverBackgroundColor: [
            '#FFE5B4',
            '#C1E1C1'

          ],
          data: [count, data.length - count]
        }
      ]
    }

    const stateResp = {
      labels: ['Normal', 'Crackles', 'Wheezes', 'Crackles & Wheezes'],
      datasets: [
        {
          label: 'Respiratory Test Report',
          backgroundColor: [
            '#FFA600',
            '#FF6361',
            '#BC5090',
            '#58508D'
          ],
          hoverBackgroundColor: [
            '#2E8B57',
            '#FF69B4',
            '#E97451',
            '#8B0000'
          ],
          data: [countNormal, countCrackles, countWheezes, countBoth]
        }
      ]
    }

    this.setState(
      {
        stateFall: stateFall,
        stateResp: stateResp
      }
    );


  }

  render() {
    const component_style = {
      width: "300px",
      height: "20px",
      paddingTop: "15px",
      paddingRight: "15px",
      paddingBottom: "15px",
      textAlign: "left",
      paddingLeft: "20px",
      marginTop: "25px"

    };
    const table_style = {
      marginLeft: "50px",
      marginTop: "16px"
    }
    const buttonStyle = {
      margin: '10px 10px 10px 0'
    };
    return (

      <div>
        <h3 >Summary Report</h3>
        <br />
        <div class="row">
          <div class="col-md-6">
            <table style={table_style}>
              <tr >
                <td style={component_style}>
                  Fall Tests Taken:
            </td>
                <td >
                  {this.state.fall_tests}
                </td>
              </tr>
              <tr >
                <td style={component_style}>
                  Respiratory Tests Taken:
            </td>
                <td>
                  {this.state.resp_tests}
                </td >
              </tr>
              <tr >
                <td style={component_style}>
                  Respiratory Status:
            </td>
                <td>
                  {this.state.resp_last_test_result}
                </td>
              </tr >
              <tr >
                <td style={component_style}>
                  Latest Fall results :
            </td>
                <td>
                  {this.state.fall_last_test_result}
                </td>
              </tr>

              <tr >
                <td style={component_style}>
                  Last Fall Test Taken on :
            </td>
                <td>
                  {this.state.fall_last_test_time}
                </td>
              </tr>
              <tr >
                <td style={component_style}>
                  Last Respiratory Test Taken on :
            </td>
                <td>
                  {this.state.resp_last_test_time}
                </td>
              </tr>

            </table>
          </div>

          <div class="col-md-6" >
            <div class="row" >
              <Pie
                data={this.state.stateResp}
                options={{
                  title: {
                    display: true,
                    text: 'Respiratory Report',
                    fontSize: 16
                  },
                  legend: {
                    display: true,
                    position: 'bottom'
                  }
                }}
                height='100%'
              />
            </div>
            <br />
            <div class="row">
              <Pie
                data={this.state.stateFall}

                options={{
                  title: {
                    display: true,
                    text: 'Fall Report',
                    fontSize: 16
                  },

                  legend: {
                    display: true,
                    position: 'bottom'
                  }
                }}
                height='100%'
              />
            </div>
          </div>
        </div>
        <div class="row" style={{ marginLeft: 60 }}>
            <NavLink className="btn btn-primary" style={buttonStyle} to="/result">
            View Test History
        </NavLink>
        </div>
      </div>
    );
  }
}

export default Dashboard;
