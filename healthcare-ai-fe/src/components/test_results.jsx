import React, { Component } from "react";
import {db} from "../conf/firebase_db.js";
import firebase from "firebase";
import {Pie, Doughnut} from 'react-chartjs-2';
import "../styling/test_results.css";

class TestResults extends Component {
  constructor(props){
    super(props)
    this.state = {
      fall_tests:0,
      respiratory_tests:0,
      fall_last_test_time:"",
      respiratory_last_test_time:new Date(),
      fall_last_test_result:"",
      respiratory_last_test_result:"",
      fallData:[],
      respData:[]

    }
  }
  async componentWillMount(){
    const fall_collection = db.collection("fall")
    //Get data from Fall Documents
    var fall_data = await fall_collection.where("email","==",firebase.auth().currentUser.email).get();
    const data = fall_data.docs.map(doc => doc.data());
    const respiratory_collection = db.collection("respiratory")
    var respiratory_data = await respiratory_collection.where("email", "==",firebase.auth().currentUser.email).get();
    const resp_data = respiratory_data.docs.map(doc => doc.data());
    this.setState(
        {
          fallData: data,
          respData: resp_data
        }
      );
  }

  renderTableData() {
    return this.state.fallData.map((fallData, index) => {
       const { email, result, time} = fallData //destructuring
       var d = new Date(time.seconds * 1000);
       var fallResult = "";
       if(result=="FALL" || result == "1"){
            fallResult = "Fall";
       }else{
        fallResult = "No Fall";
       }
       return (
          <tr key={email}>
            <td>{d.toLocaleString()}</td>
             <td>{fallResult}</td>
             
          </tr>
       )
    })
 }
 renderRespTableData() {
    return this.state.respData.map((respData, index) => {
       const { email, result, time} = respData //destructuring
       var d = new Date(time.seconds * 1000);
       var fallResult = "";
    //    if(result=="FALL" || result == "1"){
    //         fallResult = "Fall";
    //    }else{
    //     fallResult = "No Fall";
    //    }
       return (
          <tr key={email}>
            <td>{d.toLocaleString()}</td>
             <td>{result}</td>
             
          </tr>
       )
    })
 }

  render() {
    const component_style = {
      width: "300px",
      height: "20px",
      paddingRight: "15px",
      paddingBottom: "15px",
      textAlign: "left",
      paddingLeft: "20px",
      marginTop: "10px",
      marginBottom: "10px"
      
    };
    const table_style = {
      marginLeft: "10px",
      marginTop: "16px",
      marginRight: "10px",
      fontSize: 14,
      fontType: "Arial"
    }
    const buttonStyle = {
      margin: '10px 10px 10px 0'
    };
    if(this.renderTableData() == undefined){
        return(
            <div></div>
        )
    } else{
    return (
        <div style={table_style}>
        <div class = "row">
            <div class = "col-md-6">
        <h4 id='title' style = {component_style}>Fall Test History</h4>
            <table id='students'>
               <tbody>
               <tr>
                <th>Timestamp</th>
                   <th>Result</th>
                  
               </tr>
                  {this.renderTableData()}
               </tbody>
            </table>
        </div>
        <div class = "col-md-6">
        <h4 id='title' style = {component_style}>Respiratory Test History</h4>
            <table id='students'>
               <tbody>
               <tr>
                    <th>Timestamp</th>
                   <th>Result</th>
                  
               </tr>
                  {this.renderRespTableData()}
               </tbody>
            </table>
        </div>
        </div>
        </div>
    )
    }
}



}
export default TestResults;