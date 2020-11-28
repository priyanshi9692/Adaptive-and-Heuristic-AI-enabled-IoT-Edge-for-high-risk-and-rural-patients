import firebase from 'firebase';

const firebaseApp = firebase.initializeApp({
    apiKey: "AIzaSyCMssWHvsze0yxdJ6UDGzaeRm2KWU0s-ys",
    authDomain: "healthcareai-cmpe295.firebaseapp.com",
    databaseURL: "https://healthcareai-cmpe295.firebaseio.com/",
    projectId: "healthcareai-cmpe295"
});

const db = firebaseApp.firestore();

export {db};