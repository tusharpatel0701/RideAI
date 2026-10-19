
import { useEffect, useState } from "react";
import "./App.css";

interface Ride {
  id: number;
  passengerName: string;
  pickupAddress: string;
  destinationAddress: string;
  estimatedFare: number;
  status: string;
}

const API_URL = "http://localhost:8080/api/rides";

function App() {
  const [passengerName, setPassengerName] = useState("");
  const [pickupAddress, setPickupAddress] = useState("");
  const [destinationAddress, setDestinationAddress] = useState("");
  const [estimatedFare, setEstimatedFare] = useState("350");
  const [rides, setRides] = useState<Ride[]>([]);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  async function fetchRides() {
    try {
      const response = await fetch(API_URL);
      if (!response.ok) throw new Error("Failed to load rides");

      setRides(await response.json());
    } catch {
      setMessage("Cannot connect to backend. Check Spring Boot.");
    }
  }

  useEffect(() => {
    fetchRides();
  }, []);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setLoading(true);
    setMessage("");

    try {
      const response = await fetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          passengerName,
          pickupAddress,
          destinationAddress,
          estimatedFare: Number(estimatedFare),
        }),
      });

      if (!response.ok) throw new Error("Ride booking failed");

      const newRide: Ride = await response.json();
      setMessage(`Ride #${newRide.id} booked successfully!`);

      setPassengerName("");
      setPickupAddress("");
      setDestinationAddress("");

      await fetchRides();
    } catch {
      setMessage("Booking failed. Check your backend and database.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="app">
      <header>
        <h1>RideAI</h1>
        <p>Your ride, made smarter.</p>
      </header>

      <section className="booking">
        <h2>Book a ride</h2>

        <form onSubmit={handleSubmit}>
          <label>
            Passenger name
            <input
              value={passengerName}
              onChange={(e) => setPassengerName(e.target.value)}
              required
            />
          </label>

          <label>
            Pickup location
            <input
              value={pickupAddress}
              onChange={(e) => setPickupAddress(e.target.value)}
              placeholder="Enter pickup location"
              required
            />
          </label>

          <label>
            Destination
            <input
              value={destinationAddress}
              onChange={(e) => setDestinationAddress(e.target.value)}
              placeholder="Enter destination"
              required
            />
          </label>

          <label>
            Estimated fare (₹)
            <input
              type="number"
              min="1"
              step="0.01"
              value={estimatedFare}
              onChange={(e) => setEstimatedFare(e.target.value)}
              required
            />
          </label>

          <button type="submit" disabled={loading}>
            {loading ? "Booking..." : "Confirm ride"}
          </button>
        </form>

        {message && <p role="status">{message}</p>}
      </section>

      <section className="rides">
        <h2>Recent rides</h2>

        {rides.length === 0 ? (
          <p>No rides found.</p>
        ) : (
          rides.map((ride) => (
            <article className="ride-card" key={ride.id}>
              <h3>Ride #{ride.id}</h3>
              <p>Passenger: {ride.passengerName}</p>
              <p>From: {ride.pickupAddress}</p>
              <p>To: {ride.destinationAddress}</p>
              <p>Fare: ₹{Number(ride.estimatedFare).toFixed(2)}</p>
              <p>Status: {ride.status}</p>
            </article>
          ))
        )}
      </section>
    </main>
  );
}

export default App;
