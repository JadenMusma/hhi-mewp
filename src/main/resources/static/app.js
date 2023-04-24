function getRandomValue(min, max) {
  return Math.floor(Math.random() * (max - min)) + min;
}

const app = Vue.createApp({
  data() {
    return {
      jsonstr: '{ "type" : "OP", "danmal_id": "ID12345678", "ip_addr" : "", "mac" : "112345678912", "tag_id" : "11111111", "ul_gbn" : "L",  "latitude" : "0000000000", "longitude" : "0000000000", "event_dtm" : "00000000000000", "cr_start_yn":"Y", "event_cd" : "PF", "r1":"ON", "r2":"ON", "r3":"ON"}',
      header: ["단말기ID","상태","최초연결시간","마지막통신시간","펌웨어버전", "하드웨어버전"] ,
      connections: [],
      counter: 0,
      logMessages: []
    };
  },
  computed: {
    prettyJsonstr () {
      if (typeof this.jsonstr !== "object") {
        try {
          value = JSON.parse(this.jsonstr )
        } catch (err) {
          console.warn("value is not JSON")
          return this.jsonstr
        }

        return JSON.stringify(value , '', 4)
      }
    }
  },
  methods: {
    connect() {
      this.socket = new WebSocket("ws://"+ self.location.hostname + ":8081/ws/display");
      this.socket.onopen = () => {
        this.status = "connected";
        // this.logs.push({ event: "연결 완료: "})


        this.socket.onmessage = ({data}) => {

          data = JSON.parse(data);
          if(data['type'] === "log"){
            this.logMessages.push(data['data']);
          } else {
            this.connections = JSON.parse(data['data']);
          }
        };
      };
    },
    disconnect() {
      this.socket.close();
      this.status = "disconnected";
      this.logs = [];
    },
    send(){
      fetch("http://"+ self.location.hostname + ":8081/sendSetting", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        // body: JSON.stringify(this.jsonstr),
        body: this.jsonstr,
      }).then((response) => console.log(response));
    }
  },
  created: function () {
    this.connect();
  }
});

app.mount('#game');
