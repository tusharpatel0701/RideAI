
import LoginForm from "../components/auth/LoginForm";
import "../styles/LoginPage.css";

function LoginPage() {
  function handleLogin(email: string, password: string) {
    // Backend authentication will be connected in a later step.
    console.log("Login form submitted for:", email);
    console.log("Password entered:", password ? "Yes" : "No");
  }

  return (
    <main className="login-page">
      <section className="login-brand-panel">
        <div className="brand-logo">
          <span className="brand-icon">R</span>
          <span>RideAI</span>
        </div>

        <div className="brand-content">
          <p className="brand-eyebrow">YOUR JOURNEY, REIMAGINED</p>
          <h1>Every ride starts with a better idea.</h1>
          <p className="brand-description">
            Smarter journeys, seamless rides, and intelligent travel
            recommendations — all in one place.
          </p>
        </div>

        <div className="brand-footer">
          Intelligent mobility, made simple.
        </div>
      </section>

      <section className="login-content-panel">
        <div className="login-top-text">
          <span>RIDEAI ACCOUNT</span>
        </div>

        <div className="login-card">
          <LoginForm onSubmit={handleLogin} />
        </div>

        <p className="login-legal">
          By continuing, you agree to our Terms of Service and Privacy Policy.
        </p>
      </section>
    </main>
  );
}

export default LoginPage;
