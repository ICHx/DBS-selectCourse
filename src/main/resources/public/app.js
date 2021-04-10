var app = new Vue({
  el: "#app",
  data: {
    rollbackMode: "",
    alertVisible: false,
    alertType: 0,
    message: "Hello",
    selected: "",
    courseList: [],
    activeBtn: 'btn-get',
    serverResponse: {
      success: 0,
      details: "",
    },
  },
  mounted() {
    fetch("/isdebug").then(
      (r) => r.text().then(
        (t) => {
          if (t == 1) {
            this.rollbackMode = "rollbackMode: ON";
          }
        }
      )
    );
    this.resetBtn();
  },
  computed: {
    cols: function () {
      if (this.courseList.length == 0) return [];
      return Object.keys(this.courseList[0]);
    },
    alertObject: function () {
      // todo : wip
      return {
        invisible: this.alertVisible == false,
        "alert alert-secondary": this.alertVisible == true,
        //fail
        "alert alert-success": this.serverResponse.success == 1,
        "alert alert-warning": this.serverResponse.success == 2,
        "alert alert-danger": this.serverResponse.success == -2,
        // -2: unhandled error

      };
    },
  },
  methods: {
    clearList: function () {
      this.courseList = [];
    },

    //! query functions

    getAllCourses: function () {
      this.clearList();
      fetch("/courses")
        .then((response) => response.json())
        .then((courseList) => (this.courseList = courseList));

      this.resetBtn();
      document.getElementById("btn-add").style.visibility = "visible";
      this.activeBtn = 'btn-get';
    },
    getHistoryCourses: function () {
      if (!localStorage.User) {
        alert("Not logged in");
        login();
        return;
      }
      this.clearList();
      fetch("/myHistoryCourses?User=" + User)
        .then((response) => response.json())
        .then((courseList) => (this.courseList = courseList));

      this.resetBtn();
      this.activeBtn = 'btn-hist';
    },
    getMyCourses: function () {
      if (!localStorage.User) {
        alert("Not logged in");
        login();
        return;
      }
      this.clearList();
      fetch("/myCourses?User=" + User)
        .then((response) => response.json())
        .then((courseList) => (this.courseList = courseList));

      this.resetBtn();
      document.getElementById("btn-drop").style.visibility = "visible";
      this.activeBtn = 'btn-txn';
    },

    //! add/drop submission

    dropCourse: function () {
      if (!localStorage.User) {
        alert("Not logged in");
        return;
      }
      if (this.selected === "") return;

      fetch("/drop/" + this.selected + "?User=" + User)
        .then((response) => response.json())
        .then((jsonreply) => {
          this.serverResponse = jsonreply;
        });

      this.alertVisible = true;
    },
    addCourse: function () {
      if (!localStorage.User) {
        alert("Not logged in");
        return;
      }
      if (this.selected === "") return;

      fetch("/add/" + this.selected + "?User=" + User)
        .then((response) => response.json())
        .then((jsonreply) => {
          this.serverResponse = jsonreply;
        });

      this.alertVisible = true;
    },

    // auxilary functions

    resetBtn: function () {
      this.selected = "";
      document.getElementById("btn-add").style.visibility = "hidden";
      document.getElementById("btn-drop").style.visibility = "hidden";

      this.alertVisible = false;
      this.serverResponse = {
        success: 0,
        details: "",
      };
    },
    // ref: https://medium.com/chloelo925/vue-json-v-for-75f2e9d93a1e
    logout: function () {
      localStorage.removeItem("User");
      location.reload();
    },



  }, //end of methods
});

let User = null; //uses as Json parameters, global
{
  let extractId = null; //uses as login, extracted from localStore

  function login() {
    //to login as some user, password mechanisms are not implemented
    //if required to, they are hashed as a combination of user and password
    //If you assign a value to a variable that has not been declared, it will automatically become a GLOBAL variable.

    var result = 0;

    if (localStorage.User) {
      extractId = localStorage.User;
      validate("true");
      return;
    } else {
      extractId = prompt("Please enter NETID (eg: 3001)");
      fetch("/login/" + extractId)
        .then((response) => response.text())
        .then((text) => {
          result = text;

          validate(result);
        });
    }
  }

  function validate(result) {
    console.log("validate result is " + result);
    if (result == "true") {
      User = extractId; //important
      app.message = "Welcome " + extractId;
      localStorage.User = extractId;
      //    client-side cookie like stuff.
      //    In real world would be storing hashed token instead
      document.getElementById("logout-btn").style.visibility = "visible";
    } else {
      alert("Invalid NetID");
      User = null;
      app.message = "Not logged in";
    }
    console.log("User is " + User);
  }
  login();
}

