/**
 * main.js — Portfolio Website Scripts
 */

// ─── Navbar scroll effect ──────────────────────────────────────────────────
const navbar = document.querySelector('.navbar-custom');
window.addEventListener('scroll', () => {
  if (!navbar) return;
  if (window.scrollY > 60) {
    navbar.classList.add('scrolled');
  } else {
    navbar.classList.remove('scrolled');
  }
});

// ─── Skill bar animation (Intersection Observer) ──────────────────────────
const skillBars = document.querySelectorAll('.skill-bar-fill');
if (skillBars.length > 0) {
  const skillObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const bar = entry.target;
        bar.style.width = bar.getAttribute('data-width');
        skillObserver.unobserve(bar);
      }
    });
  }, { threshold: 0.3 });
  skillBars.forEach(bar => skillObserver.observe(bar));
}

// ─── Portfolio filter ──────────────────────────────────────────────────────
const filterBtns = document.querySelectorAll('.filter-btn');
const projectItems = document.querySelectorAll('.project-item');

filterBtns.forEach(btn => {
  btn.addEventListener('click', () => {
    filterBtns.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    const filter = btn.getAttribute('data-filter');
    projectItems.forEach(item => {
      const category = item.getAttribute('data-category');
      if (filter === 'all' || category === filter) {
        item.style.display = 'block';
        item.style.animation = 'fadeInUp 0.4s ease forwards';
      } else {
        item.style.display = 'none';
      }
    });
  });
});

// ─── Contact form ──────────────────────────────────────────────────────────
const contactForm = document.getElementById('contact-form');
if (contactForm) {
  contactForm.addEventListener('submit', (e) => {
    e.preventDefault();

    const name    = document.getElementById('name')?.value.trim() ?? '';
    const email   = document.getElementById('email')?.value.trim() ?? '';
    const message = document.getElementById('message')?.value.trim() ?? '';

    // Clear previous alerts
    document.querySelectorAll('.form-alert').forEach(el => el.remove());

    // Required field check
    if (!name || !email || !message) {
      showFormAlert('Please fill in all required fields.', 'error');
      return;
    }

    // Email format check
    const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRe.test(email)) {
      showFormAlert('Please enter a valid email address.', 'error');
      return;
    }

    const submitBtn = contactForm.querySelector('[type="submit"]');
    if (submitBtn) {
      submitBtn.textContent = 'Sending…';
      submitBtn.disabled = true;
    }

    // Simulate submission (replace with real back-end / EmailJS / Formspree)
    setTimeout(() => {
      showFormAlert("Message sent! I'll get back to you soon.", 'success');
      contactForm.reset();
      if (submitBtn) {
        submitBtn.textContent = 'Send Message';
        submitBtn.disabled = false;
      }
    }, 1500);
  });
}

function showFormAlert(message, type) {
  const alert = document.createElement('div');
  alert.className = `form-alert form-alert-${type}`;
  alert.textContent = message;
  contactForm.insertAdjacentElement('beforebegin', alert);
  setTimeout(() => alert.remove(), 5000);
}
