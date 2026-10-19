
import { useCallback, useEffect, useState } from "react";
import "./App.css";
import LiveTracking from "./LiveTracking";

interface Ride {
  id: number;
  passengerName: string;
  pickupAddress: string;
  destinationAddress: string;
  estimatedFare: number;
  status: string;
}

const API_URL = "http://localhost:8080/api/rides";

function formatStatus(status: string) {
  return status.replace(/_/g, " ").toLowerCase();
}

function App() {
  const [passengerName, setPassengerName] = useState("");
  const [pickupAddress, setPickupAddress] = useState("");
  const [destinationAddress, setDestinationAddress] = useState("");
  const [estimatedFare, setEstimatedFare] = useState("350");

  const [rides, setRides] = useState<Ride[]>([]);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"success" | "error">(
    "success"
  );
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);

  const fetchRides = useCallback(async () => {
    setFetching(true);

    try {
      const response = await fetch(API_URL);

      if (!response.ok) {
        throw new Error("Unable to load rides");
      }

      const data: Ride[] = await response.json();
      setRides(data);
    } catch {
      setMessage("Cannot connect to the backend. Check Spring Boot.");
      setMessageType("error");
    } finally {
      setFetching(false);
    }
  }, []);

  useEffect(() => {
    void fetchRides();
  }, [fetchRides]);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setLoading(true);
    setMessage("");

    try {
      const response = await fetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          passengerName: passengerName.trim(),
          pickupAddress: pickupAddress.trim(),
          destinationAddress: destinationAddress.trim(),
          estimatedFare: Number(estimatedFare),
        }),
      });

      if (!response.ok) {
        throw new Error("Ride booking failed");
      }

      const newRide: Ride = await response.json();

      setMessage(`Ride #${newRide.id} booked successfully.`);
      setMessageType("success");

      setPassengerName("");
      setPickupAddress("");
      setDestinationAddress("");
      setEstimatedFare("350");

      await fetchRides();
    } catch {
      setMessage("Booking failed. Check your backend and database.");
      setMessageType("error");
    } finally {
      setLoading(false);
    }
  }

  const activeRides = rides.filter((ride) =>
    ["REQUESTED", "ACCEPTED", "ASSIGNED", "IN_PROGRESS", "STARTED"].includes(
      ride.status.toUpperCase()
    )
  );

  return (
    <main className="app-shell">
      <header className="topbar">
        <a className="brand" href="#home" aria-label="RideAI home">
          <span className="brand-icon">R</span>
          <span>Ride<span className="brand-accent">AI</span></span>
        </a>

        <nav className="topbar-nav">
          <a className="nav-link active" href="#home">Dashboard</a>
          <a className="nav-link" href="#tracking">Live tracking</a>
          <a className="nav-link" href="#rides">My rides</a>
        </nav>

        <div className="topbar-right">
          <span className="online-indicator" />
          <span className="connection-label">Ride dashboard</span>
        </div>
      </header>

      <div className="page-container" id="home">
        <section className="welcome-section">
          <div>
            <p className="eyebrow">YOUR JOURNEY, SIMPLIFIED</p>
            <h1>Where are we going?</h1>
            <p className="welcome-description">
              Book your next ride and keep track of every journey.
            </p>
          </div>

          <button
            className="secondary-button"
            type="button"
            onClick={() => void fetchRides()}
            disabled={fetching}
          >
            <span className={fetching ? "refresh-icon spinning" : "refresh-icon"}>
              ↻
            </span>
            {fetching ? "Refreshing..." : "Refresh rides"}
          </button>
        </section>

        {message && (
          <div
            className={`notice ${messageType}`}
            role="status"
            aria-live="polite"
          >
            <span>{messageType === "success" ? "✓" : "!"}</span>
            {message}
            <button
              type="button"
              className="notice-close"
              onClick={() => setMessage("")}
              aria-label="Dismiss message"
            >
              ×
            </button>
          </div>
        )}

        <section className="dashboard-grid">
          <section className="panel booking-panel">
            <div className="panel-heading">
              <div>
                <span className="section-kicker">GET MOVING</span>
                <h2>Book a ride</h2>
                <p>Enter your trip details to get started.</p>
              </div>
              <div className="heading-icon" aria-hidden="true">↗</div>
            </div>

            <form onSubmit={handleSubmit} className="booking-form">
              <label className="field">
                <span>Passenger name</span>
                <input
                  value={passengerName}
                  onChange={(e) => setPassengerName(e.target.value)}
                  placeholder="Enter your name"
                  autoComplete="name"
                  maxLength={100}
                  required
                />
              </label>

              <div className="route-fields">
                <div className="route-indicator" aria-hidden="true">
                  <span className="route-dot pickup-dot" />
                  <span className="route-line" />
                  <span className="route-dot destination-dot" />
                </div>

                <div className="route-inputs">
                  <label className="field">
                    <span>Pickup location</span>
                    <input
                      value={pickupAddress}
                      onChange={(e) => setPickupAddress(e.target.value)}
                      placeholder="Where should we pick you up?"
                      maxLength={255}
                      required
                    />
                  </label>

                  <label className="field">
                    <span>Destination</span>
                    <input
                      value={destinationAddress}
                      onChange={(e) => setDestinationAddress(e.target.value)}
                      placeholder="Where do you want to go?"
                      maxLength={255}
                      required
                    />
                  </label>
                </div>
              </div>

              <label className="field">
                <span>Estimated fare</span>
                <div className="fare-input">
                  <span className="currency-symbol">₹</span>
                  <input
                    type="number"
                    min="1"
                    max="1000000"
                    step="0.01"
                    value={estimatedFare}
                    onChange={(e) => setEstimatedFare(e.target.value)}
                    required
                  />
                  <span className="fare-caption">INR</span>
                </div>
              </label>

              <div className="fare-summary">
                <span>Estimated trip fare</span>
                <strong>
                  ₹
                  {Number(estimatedFare || 0).toLocaleString("en-IN", {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  })}
                </strong>
              </div>

              <button
                className="primary-button"
                type="submit"
                disabled={loading}
              >
                {loading ? (
                  <><span className="button-spinner" /> Booking your ride...</>
                ) : (
                  <>Confirm ride <span aria-hidden="true">→</span></>
                )}
              </button>

              <p className="form-footnote">
                Your trip details will be saved to your RideAI backend.
              </p>
            </form>
          </section>

          <section className="panel tracking-panel" id="tracking">
            <div className="panel-heading tracking-heading">
              <div>
                <span className="section-kicker">ON THE MOVE</span>
                <h2 >Live tracking</h2>
                <p>Follow your driver's location in real time.</p>
              </div>
              <span className="live-badge">
                <span className="live-dot" />
                LIVE
              </span>
            </div>

            <div className="tracking-content">
              <LiveTracking />
            </div>

            <div className="tracking-footer">
              <span className="tracking-footer-icon">⌖</span>
              <div>
                <strong>Driver location</strong>
                <p>Live updates depend on an active driver connection.</p>
              </div>
            </div>
          </section>
        </section>

        <section className="rides-section" id="rides">
          <div className="rides-heading">
            <div>
              <span className="section-kicker">YOUR ACTIVITY</span>
              <h2>Recent rides</h2>
              <p>Keep an eye on your journeys and their status.</p>
            </div>

            <div className="ride-count">
              <span>{rides.length}</span>
              {rides.length === 1 ? "total ride" : "total rides"}
            </div>
          </div>

          <div className="ride-stats">
            <div className="stat-card">
              <span className="stat-label">All rides</span>
              <strong>{rides.length}</strong>
              <span className="stat-caption">Total bookings</span>
            </div>

            <div className="stat-card">
              <span className="stat-label">Active rides</span>
              <strong>{activeRides.length}</strong>
              <span className="stat-caption">Currently underway or pending</span>
            </div>

            <div className="stat-card">
              <span className="stat-label">Total estimated fares</span>
              <strong>
                ₹
                {rides.reduce(
                  (total, ride) => total + (Number(ride.estimatedFare) || 0),
                  0
                ).toLocaleString("en-IN", {
                  maximumFractionDigits: 2,
                })}
              </strong>
              <span className="stat-caption">Across all loaded rides</span>
            </div>
          </div>

          {fetching && rides.length === 0 ? (
            <div className="empty-state">
              <span className="empty-icon">↻</span>
              <h3>Loading your rides</h3>
              <p>Getting your latest trip details.</p>
            </div>
          ) : rides.length === 0 ? (
            <div className="empty-state">
              <span className="empty-icon">↗</span>
              <h3>Your next journey starts here</h3>
              <p>Book your first ride and it will appear here.</p>
              <a className="empty-link" href="#home">Book a ride →</a>
            </div>
          ) : (
            <div className="ride-list">
              {rides.map((ride) => {
                const status = ride.status.toUpperCase();
                const statusClass = status.toLowerCase().replace(/_/g, "-");

                return (
                  <article className="ride-card" key={ride.id}>
                    <div className="ride-card-top">
                      <div className="ride-number">
                        <span className="ride-car-icon">↗</span>
                        <div>
                          <strong>Ride #{ride.id}</strong>
                          <span>{ride.passengerName}</span>
                        </div>
                      </div>

                      <span className={`status-badge ${statusClass}`}>
                        <span className="status-dot" />
                        {formatStatus(ride.status)}
                      </span>
                    </div>

                    <div className="ride-route">
                      <div className="ride-route-marker">
                        <span className="mini-dot pickup-dot" />
                        <span className="mini-route-line" />
                        <span className="mini-dot destination-dot" />
                      </div>

                      <div className="ride-route-addresses">
                        <div className="ride-address">
                          <span>Pickup</span>
                          <strong>{ride.pickupAddress}</strong>
                        </div>
                        <div className="ride-address">
                          <span>Destination</span>
                          <strong>{ride.destinationAddress}</strong>
                        </div>
                      </div>
                    </div>

                    <div className="ride-card-bottom">
                      <div className="ride-fare">
                        <span>Estimated fare</span>
                        <strong>
                          ₹{Number(ride.estimatedFare).toLocaleString("en-IN", {
                            minimumFractionDigits: 2,
                            maximumFractionDigits: 2,
                          })}
                        </strong>
                      </div>

                      {["REQUESTED", "ACCEPTED", "ASSIGNED", "IN_PROGRESS", "STARTED"].includes(status) && (
                        <a className="track-link" href="#tracking">
                          Track ride <span>→</span>
                        </a>
                      )}
                    </div>
                  </article>
                );
              })}
            </div>
          )}
        </section>

        <footer className="app-footer">
          <a className="footer-brand" href="#home">RideAI</a>
          <span>Smarter journeys start here.</span>
          <a href="#home">Back to top ↑</a>
        </footer>
      </div>
    </main>
  );
}

export default App;
