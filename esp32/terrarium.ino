#include <ESP8266WiFi.h>
#include <NTPClient.h>
#include <DHT.h>
#include <Servo.h>

bool ledStatus[] = {0, 0, 0};
const int ledPins[] = {14, 12, 13};
const int DHTPIN = 4;
const int SERVOPIN = 5;
const char* ssid = "MI";
const char* password = "1285012850";
int graftemp[10];
int grafhyd[10];
int idgraf = 0;
bool flag = 0;
bool flagtime = 0;
int servoPosition = 0;

int temperatureC = 0;
WiFiServer server(80);
DHT dht(DHTPIN, DHT11);

struct RelaySchedule {
  int HourIn;
  int MinuteIn;
  int HourOut;
  int MinuteOut;
};
RelaySchedule alarms[3] = {
  {10, 0, 22, 0}, // Relay 1: 10:00 - 22:00
  {-1, -1, -1, -1},   // Relay 2: null (disabled)
  {10, 0, 22, 0}  // Relay 3: 10:00 - 22:00
};

struct EveryHour {
  int id;
  int time;
  int timein;
  int timeout;
  bool status;
};
EveryHour everyhour;
Servo myServo;

void setup() {
  dht.begin();
  Serial.begin(9600);

  myServo.attach(SERVOPIN);

  memset(graftemp, 0, sizeof(graftemp));
  memset(grafhyd, 0, sizeof(grafhyd));

  for (int i = 0; i < 3; i++) {
    pinMode(ledPins[i], OUTPUT);
    digitalWrite(ledPins[i], LOW);
  }

  delay(1000);
  digitalWrite(ledPins[2], HIGH);
  delay(1000);
  digitalWrite(ledPins[2], LOW);
  delay(10);

  myServo.write(102);
  delay(7500);
  myServo.write(90);

  configTime(3 * 3600, 0, "pool.ntp.org", "time.nist.gov");

  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println();
  Serial.println("WiFi connected");

  delay(200);
  for (int i = 0; i < 3; i++) {
    digitalWrite(ledPins[i], HIGH);
  }
  delay(200);
  for (int i = 0; i < 3; i++) {
    digitalWrite(ledPins[i], LOW);
  }

  graftemp[idgraf] = static_cast<int>(dht.readTemperature());
  grafhyd[idgraf] = static_cast<int>(dht.readHumidity());
  idgraf++;

  server.begin();
  Serial.println("Server started");
  Serial.println(WiFi.localIP());
}

void loop() {
  time_t now = time(nullptr);
  struct tm *timeinfo = localtime(&now);

  if (timeinfo->tm_sec == 0) {
    if (flagtime == 0){
      for (int i = 0; i < 3; i++) {
        if (timeinfo->tm_hour == alarms[i].HourIn && timeinfo->tm_min == alarms[i].MinuteIn) {
          changeRele(i);
        } else if (timeinfo->tm_hour == alarms[i].HourOut && timeinfo->tm_min == alarms[i].MinuteOut) {
          changeRele(i);
        }
      }

      if (timeinfo->tm_min == 0) { 
        flag = 1;
        if (everyhour.status) {
          if (timeinfo->tm_hour >= everyhour.timein && timeinfo->tm_hour < everyhour.timeout) {
            changeRele(everyhour.id);
          } 
        }
      } else if (everyhour.status && ledStatus[everyhour.id] && timeinfo->tm_min == everyhour.time) {
        changeRele(everyhour.id);
      }
      flagtime = 1;
      delay(1000); 
    }
  } else {
    flagtime = 0;
  }


  if (flag == 1) {
    for (int i = 0; i < 9; i++) {
      graftemp[i] = graftemp[i + 1];
      grafhyd[i] = grafhyd[i + 1];
    }

    graftemp[9] = static_cast<int>(dht.readTemperature());
    grafhyd[9] = static_cast<int>(dht.readHumidity());

    flag = 0;
  }

  WiFiClient client = server.available();
  if (!client) {
    return;
  }

  Serial.println("New client");
  while (!client.available()) {
    delay(1);
  }

  String req = client.readStringUntil('\r');
  Serial.println(req);


  String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n";

  String clientreq = handleRequest(req, client);

  response += clientreq;


  client.flush();

  client.print(response);
  delay(1);
  Serial.println("Client disconnected");
}

String getArrayAsString(int arr[], int size) {
  String result = "";
  for (int i = 0; i < size; i++) {
    result += String(arr[i]);
    if (i < size - 1) {
      result += ",";
    }
  }
  return result;
}

String handleRequest(String req, WiFiClient client) {
  String response = "";
  if (req.indexOf("/led") != -1) {
    int ledIndex = req.substring(req.indexOf("led") + 3).toInt() - 1;
    changeRele(ledIndex);
    response += "rele: " + String(ledStatus[ledIndex]) + "\r\n";
  } else if (req.indexOf("/servo") != -1) {
    int position = req.substring(req.indexOf("servo") + 5).toInt();
    Serial.println(position);
    response += "position: " + String(position) + "\r\n";
    if (position == 0) {
      changeServo(1);
    } else if (position == 2500) {
      changeServo(2500);
    } else if (position == 4700) {
      changeServo(4700);
    } else if (position == 7500) {
      changeServo(7500);
    }
  } else if (req.indexOf("/temperature") != -1) {
    response += "Datchikt: " + String(static_cast<int>(dht.readTemperature())) + "\r\n";
    response += "Datchikh: " + String(static_cast<int>(dht.readHumidity())) + "\r\n";
  } else if (req.indexOf("/task") != -1) {
    response += "task1: " + String(alarms[0].HourIn) + ":" + String(alarms[0].MinuteIn) + "/" + String(alarms[0].HourOut) + ":" + String(alarms[0].MinuteOut) + "\r\n";
    response += "task2: " + String(alarms[1].HourIn) + ":" + String(alarms[1].MinuteIn) + "/" + String(alarms[1].HourOut) + ":" + String(alarms[1].MinuteOut) + "\r\n";
    response += "task3: " + String(alarms[2].HourIn) + ":" + String(alarms[2].MinuteIn) + "/" + String(alarms[2].HourOut) + ":" + String(alarms[2].MinuteOut) + "\r\n";
    response += "hour: " + String(everyhour.timein) + "-" + String(everyhour.timeout) + "/" + String(everyhour.time) + "\r\n";
  } else if (req.indexOf("/status") != -1) {
    response += "rele1: " + String(ledStatus[0]) + "\r\n";
    response += "rele2: " + String(ledStatus[1]) + "\r\n";
    response += "rele3: " + String(ledStatus[2]) + "\r\n";
  } else if (req.indexOf("/graf") != -1) {
    response += "TemperatureGraf: " + getArrayAsString(graftemp, 10) + "\r\n";
    response += "HumidityGraf: " + getArrayAsString(grafhyd, 10) + "\r\n";
  } else if (req.indexOf("/everyhour") != -1) {
    everyhour.id = req.substring(req.indexOf("id=") + 3, req.indexOf("&")).toInt();
    everyhour.status = req.substring(req.indexOf("status=") + 7, req.indexOf("&timein=")).toInt();
    everyhour.timein = req.substring(req.indexOf("timein=") + 7, req.indexOf("&timeout=")).toInt();
    everyhour.timeout = req.substring(req.indexOf("timeout=") + 8, req.indexOf("&time=")).toInt();
    everyhour.time = req.substring(req.indexOf("time=") + 5).toInt();
    Serial.print("EveryHour updated: ID=");
    Serial.print(everyhour.id);
    Serial.print(", Status=");
    Serial.print(everyhour.status);
    Serial.print(", TimeIn=");
    Serial.print(everyhour.timein);
    Serial.print(", Timeout=");
    Serial.print(everyhour.timeout);
    Serial.print(", Time=");
    Serial.println(everyhour.time);

    changeRele(2);
    delay(200);
    changeRele(2);
    delay(10);


  } else if (req.indexOf("/relay") != -1) {
    int id = req.substring(req.indexOf("id=") + 3, req.indexOf("&")).toInt();
    alarms[id].HourIn = req.substring(req.indexOf("hourin=") + 7, req.indexOf("&minin=")).toInt();
    alarms[id].MinuteIn = req.substring(req.indexOf("minin=") + 6, req.indexOf("&hourout=")).toInt();
    alarms[id].HourOut = req.substring(req.indexOf("hourout=") + 8, req.indexOf("&minout=")).toInt();
    alarms[id].MinuteOut = req.substring(req.indexOf("minout=") + 7).toInt();
    Serial.print("Relay updated: ID=");
    Serial.print(id);
    Serial.print(", HourIn=");
    Serial.print(alarms[id].HourIn);
    Serial.print(", MinIn=");
    Serial.print(alarms[id].MinuteIn);
    Serial.print(", HourOut=");
    Serial.print(alarms[id].HourOut);
    Serial.print(", MinOut=");
    Serial.println(alarms[id].MinuteOut);

    changeRele(2);
    delay(200);
    changeRele(2);
    delay(10);


  } else {
    Serial.println("Invalid request");
    client.stop();
  }
  return response;
}

void changeServo(int position) {
  int moveDuration = abs(position - servoPosition);
  if (position > servoPosition) {
    myServo.write(80);
  } else {
    myServo.write(100);
  }
  delay(moveDuration);
  myServo.write(90);
  servoPosition = position;
}

void changeRele(int ledIndex) {
  if (ledIndex < 0 || ledIndex >= 3) {
    return;
  }
  delay(10);
  Serial.println(ledStatus[ledIndex]);
  delay(10);
  digitalWrite(ledPins[ledIndex], ledStatus[ledIndex] ? LOW : HIGH);
  delay(10);
  ledStatus[ledIndex] = !ledStatus[ledIndex];
}
