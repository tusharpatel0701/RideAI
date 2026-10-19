
import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";

interface Location {
  driverId: number;
  latitude: number;
  longitude: number;
}

export default function LiveTracking() {
  const clientRef = useRef<Client | null>(null);
  const [connected, setConnected] = useState(false);
  const [location, setLocation] = useState<Location | null>(null);
  const [message, setMessage] = useState("");

  useEffect(() => {
    const client = new Client({
      brokerURL: "ws://localhost:8080/ws",
      reconnectDelay: 3000,

      onConnect: () => {
        setConnected(true);
        setMessage("WebSocket connected successfully");

        client.subscribe(
          "/topic/drivers/1/location",
          (frame) => {
            setLocation(JSON.parse(frame.body) as Location);
          }
        );
      },

      onWebSocketClose: () => setConnected(false),

      onWebSocketError: () => {
        setMessage("WebSocket connection error");
      },

      onStompError: (frame) => {
        setMessage(frame.headers.message ?? "STOMP error");
      },
    });

    clientRef.current = client;
    client.activate();

    return () => {
      clientRef.current = null;
      void client.deactivate();
    };
  }, []);

  function sendTestLocation() {
    const client = clientRef.current;

    if (!client?.connected) {
      setMessage("WebSocket is not connected");
      return;
    }

    const testLocation = {
      latitude: 28.4800,
      longitude: 76.5100,
    };

    client.publish({
      destination: "/app/drivers/1/location",
      body: JSON.stringify(testLocation),
    });

    setMessage("Test location sent");
  }

  return (
    <section>
      <h2>Live Driver Tracking</h2>

      <p>
        Connection: {connected ? "Connected" : "Disconnected"}
      </p>

      <button onClick={sendTestLocation} disabled={!connected}>
        Send Test Location
      </button>

      <p role="status">{message}</p>

      <h3>Latest Driver Location</h3>

      {location ? (
        <>
          <p>Driver ID: {location.driverId}</p>
          <p>Latitude: {location.latitude}</p>
          <p>Longitude: {location.longitude}</p>
        </>
      ) : (
        <p>Waiting for location updates...</p>
      )}
    </section>
  );
}
