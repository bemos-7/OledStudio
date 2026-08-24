#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>
#include <Adafruit_SSD1306.h>

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

WiFiClient client;
HTTPClient http;

const char* ssid = "your-wifi-name";
const char* password = "your-wifi-password";
const char* serverName = "http://set-your-pc-ip-address:8006"; 
const char* handler = "/api/esp/image";

#define BITMAP_SIZE 1024
uint8_t dynamicBitmap[BITMAP_SIZE];

void setup() {
  Serial.begin(115200);

  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println("SSD1306 allocation failed");
    for (;;);
  }
  display.clearDisplay();
  display.display();

  Serial.println("Connecting to WiFi...");
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }

  Serial.println("\nConnected to WiFi");
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 0);
  display.println("WiFi Connected!");
  display.display();
  delay(2000);
}

void loop() {
  if (WiFi.status() == WL_CONNECTED) {
    String fullUrl = String(serverName) + String(handler);
    http.begin(client, fullUrl);
    int httpCode = http.GET();

    if (httpCode == 200) {
      Serial.println("HTTP GET request successful");
      String payload = http.getString();
      
      if (parseJsonImage(payload)) {
        displayDynamicLogo();
      } else {
        displayError("JSON Parse Error");
      }
    } else {
      Serial.println("Error on HTTP request: " + String(httpCode));
      displayError("HTTP Error: " + String(httpCode));
    }

    http.end();
  } else {
    Serial.println("WiFi disconnected");
    displayError("WiFi Lost");
  }

  delay(10000);
}

bool parseJsonImage(String jsonPayload) {
  JsonDocument doc;

  DeserializationError error = deserializeJson(doc, jsonPayload);
  if (error) {
    Serial.print("deserializeJson() failed: ");
    Serial.println(error.c_str());
    return false;
  }

  JsonArray bytesArray = doc["bytes"];
  if (bytesArray.isNull()) {
    Serial.println("Error: 'bytes' key not found or null");
    return false;
  }

  memset(dynamicBitmap, 0, BITMAP_SIZE);

  size_t bytesToRead = bytesArray.size();
  if (bytesToRead > BITMAP_SIZE) {
    bytesToRead = BITMAP_SIZE;
  }

  for (size_t i = 0; i < bytesToRead; i++) {
    dynamicBitmap[i] = bytesArray[i].as<uint8_t>();
  }

  return true;
}

void displayDynamicLogo() {
  display.clearDisplay();

  display.drawBitmap(0, 0, dynamicBitmap, SCREEN_WIDTH, SCREEN_HEIGHT, SSD1306_WHITE);
  
  display.display();
}

void displayError(String message) {
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 0);
  display.println("Status:");
  display.println(message);
  display.display();
}
