
import RegisterForm from "../components/auth/RegisterForm";
import "../styles/LoginPage.css";

function RegisterPage() {
  function handleRegister(
    name: string,
    email: string,
    password: string
  ) {
    // Backend registration will be connected later.
    console.log("Registration form submitted for:", name, email);
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
          <p className="brand-eyebrow">MOVE SMARTER</p>
          <h1>Your next journey starts here.</h1>
          <p className="brand-description">
            Create your account to explore a smarter and more seamless
            ride experience.
          </p>
        </div>

        <div className="brand-footer">
          Intelligent mobility, made simple.
        </div>
      </section>

      <section className="login-content-panel">
        <div className="login-top-text">JOIN RIDEAI</div>

        <div className="login-card">
          <RegisterForm onSubmit={handleRegister} />
        </div>

        <p className="login-legal">
          By creating an account, you agree to our Terms of Service
          and Privacy Policy.
        </p>
      </section>
    </main>
  );
}

export default RegisterPage;
