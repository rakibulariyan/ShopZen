/**
 * ShopZen Official Website — Vanilla JavaScript
 * Clean, lightweight, zero dependencies, accessible.
 */

document.addEventListener('DOMContentLoaded', () => {
  initMobileNav();
  initPreviewTabs();
  initFaqAccordions();
  initStoreBadgeModal();
  updateCurrentYear();
});

/**
 * Mobile Navigation Drawer
 */
function initMobileNav() {
  const toggleBtn = document.querySelector('.mobile-menu-btn');
  const drawer = document.querySelector('.mobile-nav-drawer');
  const closeBtn = document.querySelector('.mobile-nav-close');
  const navLinks = document.querySelectorAll('.mobile-nav-link');

  if (!toggleBtn || !drawer) return;

  function openMenu() {
    drawer.classList.add('open');
    toggleBtn.setAttribute('aria-expanded', 'true');
    drawer.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
    if (closeBtn) closeBtn.focus();
  }

  function closeMenu() {
    drawer.classList.remove('open');
    toggleBtn.setAttribute('aria-expanded', 'false');
    drawer.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
    toggleBtn.focus();
  }

  toggleBtn.addEventListener('click', () => {
    const isOpen = drawer.classList.contains('open');
    if (isOpen) {
      closeMenu();
    } else {
      openMenu();
    }
  });

  if (closeBtn) {
    closeBtn.addEventListener('click', closeMenu);
  }

  // Close when clicking overlay backdrop
  drawer.addEventListener('click', (e) => {
    if (e.target === drawer) {
      closeMenu();
    }
  });

  // Close with Escape key
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && drawer.classList.contains('open')) {
      closeMenu();
    }
  });

  // Close when clicking nav link
  navLinks.forEach(link => {
    link.addEventListener('click', () => {
      closeMenu();
    });
  });
}

/**
 * Interactive App Preview Tab Switcher
 */
function initPreviewTabs() {
  const tabs = document.querySelectorAll('.preview-tab-btn');
  const mockupImg = document.getElementById('previewMockupImg');
  const badge = document.getElementById('previewBadge');
  const heading = document.getElementById('previewHeading');
  const copy = document.getElementById('previewCopy');
  const bullets = document.getElementById('previewBullets');

  if (!tabs.length || !mockupImg) return;

  const previewData = {
    dashboard: {
      badge: 'LIST MANAGEMENT',
      title: 'Calm, Organized Shopping Lists',
      copy: 'View all your active shopping lists at a glance. Track item counts, completion percentages, and live spending against target budgets with zero visual clutter.',
      bullets: [
        'Organize multiple lists (Groceries, Farmers Market, Dinner Party).',
        'Visual progress indicators show purchased vs pending items.',
        'Supports multiple ISO 4217 currencies per individual list.',
        'Duplicate any list in one tap to quickly reuse staples.'
      ],
      src: '/assets/images/app-preview/preview-dashboard.svg',
      alt: 'ShopZen shopping list overview dashboard on Android'
    },
    items: {
      badge: 'ITEM ORGANIZATION',
      title: 'Categorized Items & Custom Units',
      copy: 'Organize items by natural grocery aisles: Produce, Dairy, Bakery, Pantry, and Meat. Add quantities with custom units (kg, lb, box, bunch) and estimated prices.',
      bullets: [
        'Predefined grocery categories streamline in-store navigation.',
        'Detailed item notes for specific brands or dietary preferences.',
        'Instant search and filter to find items in large grocery lists.',
        'Clear separation of estimated vs actual unit costs.'
      ],
      src: '/assets/images/app-preview/preview-items.svg',
      alt: 'ShopZen shopping list items categorized view'
    },
    market: {
      badge: 'IN-STORE MARKET MODE',
      title: 'Engineered for One-Handed Shopping',
      copy: 'Step into the grocery aisle with confidence. Market Mode provides giant 48dp tactile checkboxes, real-time in-cart counters, subtle haptics, and instant undo.',
      bullets: [
        'Large touch targets easily tapped while pushing a shopping cart.',
        'Real-time aisle HUD: counts items in cart vs pending.',
        'Quick actual price entry as you discover store discounts.',
        'Strictly zero advertisements on shopping and checkout screens.'
      ],
      src: '/assets/images/app-preview/preview-market-mode.svg',
      alt: 'ShopZen Market Mode screen with large touch targets'
    },
    budget: {
      badge: 'FINANCIAL ACCURACY',
      title: 'Real-Time Budget & Spend Tracking',
      copy: 'Stay informed without getting blocked. Set a target budget and watch your estimated vs actual spend update live with exact minor-unit arithmetic.',
      bullets: [
        'Zero floating-point rounding errors: exact integer minor units.',
        'Calm, non-blocking notifications if spending exceeds budget.',
        'Explicit disclosure of unpriced purchased items.',
        'Category spend distribution shows where your grocery money goes.'
      ],
      src: '/assets/images/app-preview/preview-budget.svg',
      alt: 'ShopZen budget summary and spend analysis screen'
    },
    history: {
      badge: 'PERMANENT RECORDS',
      title: 'Immutable Shopping History Snapshots',
      copy: 'Archive completed shopping trips into verifiable historical snapshots. Review past prices and shopping dates anytime, even if original lists are edited.',
      bullets: [
        'Atomic archive preserves exact items, prices, and timestamp.',
        'Full historical detail remains accessible offline indefinitely.',
        'Analyze long-term grocery spending trends and price inflation.',
        '100% private: stored strictly on your local device.'
      ],
      src: '/assets/images/app-preview/preview-history.svg',
      alt: 'ShopZen immutable shopping history records screen'
    }
  };

  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const tabId = tab.getAttribute('data-tab');
      const data = previewData[tabId];
      if (!data) return;

      // Update active tab button
      tabs.forEach(t => {
        t.classList.remove('active');
        t.setAttribute('aria-selected', 'false');
      });
      tab.classList.add('active');
      tab.setAttribute('aria-selected', 'true');

      // Update mockup graphic
      mockupImg.style.opacity = '0';
      setTimeout(() => {
        mockupImg.src = data.src;
        mockupImg.alt = data.alt;
        mockupImg.style.opacity = '1';
      }, 140);

      // Update text content
      if (badge) badge.textContent = data.badge;
      if (heading) heading.textContent = data.title;
      if (copy) copy.textContent = data.copy;

      if (bullets) {
        bullets.innerHTML = data.bullets.map(b => `
          <li>
            <svg viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span>${b}</span>
          </li>
        `).join('');
      }
    });
  });
}

/**
 * Accessible FAQ Accordions
 */
function initFaqAccordions() {
  const faqItems = document.querySelectorAll('.faq-item');
  if (!faqItems.length) return;

  faqItems.forEach(item => {
    const questionBtn = item.querySelector('.faq-question');
    if (!questionBtn) return;

    questionBtn.addEventListener('click', () => {
      const isActive = item.classList.contains('active');

      // Optional: close other open items for accordion behavior
      faqItems.forEach(otherItem => {
        if (otherItem !== item && otherItem.classList.contains('active')) {
          otherItem.classList.remove('active');
          const otherBtn = otherItem.querySelector('.faq-question');
          if (otherBtn) otherBtn.setAttribute('aria-expanded', 'false');
        }
      });

      if (isActive) {
        item.classList.remove('active');
        questionBtn.setAttribute('aria-expanded', 'false');
      } else {
        item.classList.add('active');
        questionBtn.setAttribute('aria-expanded', 'true');
      }
    });
  });
}

/**
 * Store Badge Release Notice Modal
 * Clear, transparent placeholder handling for unreleased app listing
 */
function initStoreBadgeModal() {
  const storeBadges = document.querySelectorAll('.store-badge-placeholder');
  if (!storeBadges.length) return;

  let modal = document.getElementById('storeNoticeModal');
  if (!modal) {
    modal = document.createElement('div');
    modal.id = 'storeNoticeModal';
    modal.className = 'store-modal-overlay';
    modal.setAttribute('role', 'dialog');
    modal.setAttribute('aria-modal', 'true');
    modal.setAttribute('aria-labelledby', 'storeModalTitle');
    modal.innerHTML = `
      <div class="store-modal-card">
        <div class="store-modal-header">
          <div class="store-modal-icon-badge">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
            </svg>
          </div>
          <button type="button" class="store-modal-close" aria-label="Close dialog">&times;</button>
        </div>
        <h3 id="storeModalTitle" class="store-modal-title">Google Play Release In Progress</h3>
        <p class="store-modal-text">
          ShopZen (v1.0.0) is currently in final Google Play Console release review. The public Google Play download link will be activated here immediately upon store approval.
        </p>
        <div class="store-modal-meta">
          <div><strong>Application ID:</strong> <code>com.barakwebsoft.shopzen</code></div>
          <div><strong>Target:</strong> Android 10+ (API 29 to 37)</div>
          <div><strong>Contact:</strong> <a href="mailto:connect@barakwebsoft.com">connect@barakwebsoft.com</a></div>
        </div>
        <button type="button" class="btn btn-primary store-modal-confirm">Got it</button>
      </div>
    `;
    document.body.appendChild(modal);

    // Inject minimal modal styles
    const style = document.createElement('style');
    style.textContent = `
      .store-modal-overlay {
        position: fixed;
        top: 0; left: 0; right: 0; bottom: 0;
        background: rgba(18, 26, 23, 0.7);
        backdrop-filter: blur(6px);
        display: flex; align-items: center; justify-content: center;
        z-index: 2000;
        padding: 20px;
        opacity: 0; visibility: hidden;
        transition: opacity 0.25s ease, visibility 0.25s ease;
      }
      .store-modal-overlay.open { opacity: 1; visibility: visible; }
      .store-modal-card {
        background: #FFFFFF;
        border-radius: 20px;
        max-width: 460px;
        width: 100%;
        padding: 28px;
        box-shadow: 0 16px 40px rgba(0,0,0,0.2);
        transform: translateY(16px);
        transition: transform 0.25s ease;
      }
      .store-modal-overlay.open .store-modal-card { transform: translateY(0); }
      .store-modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
      .store-modal-icon-badge {
        width: 44px; height: 44px; border-radius: 12px;
        background: #E8F6ED; color: #16845B;
        display: flex; align-items: center; justify-content: center;
      }
      .store-modal-close {
        background: transparent; border: none; font-size: 28px; line-height: 1;
        cursor: pointer; color: #57655D; padding: 4px;
      }
      .store-modal-title { font-size: 1.35rem; font-weight: 800; color: #17221D; margin-bottom: 12px; }
      .store-modal-text { font-size: 0.98rem; color: #57655D; line-height: 1.6; margin-bottom: 18px; }
      .store-modal-meta {
        background: #F0F4F2; border-radius: 10px; padding: 12px 14px;
        font-size: 0.85rem; color: #17221D; margin-bottom: 24px;
        display: flex; flex-direction: column; gap: 6px;
      }
      .store-modal-confirm { width: 100%; }
    `;
    document.head.appendChild(style);
  }

  function showModal() {
    modal.classList.add('open');
    document.body.style.overflow = 'hidden';
    const closeBtn = modal.querySelector('.store-modal-confirm');
    if (closeBtn) closeBtn.focus();
  }

  function hideModal() {
    modal.classList.remove('open');
    document.body.style.overflow = '';
  }

  storeBadges.forEach(badge => {
    badge.addEventListener('click', (e) => {
      e.preventDefault();
      showModal();
    });
  });

  const closeBtn = modal.querySelector('.store-modal-close');
  const confirmBtn = modal.querySelector('.store-modal-confirm');
  if (closeBtn) closeBtn.addEventListener('click', hideModal);
  if (confirmBtn) confirmBtn.addEventListener('click', hideModal);

  modal.addEventListener('click', (e) => {
    if (e.target === modal) hideModal();
  });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && modal.classList.contains('open')) hideModal();
  });
}

/**
 * Update Copyright Year
 */
function updateCurrentYear() {
  const yearEls = document.querySelectorAll('.current-year');
  const currentYear = new Date().getFullYear();
  yearEls.forEach(el => {
    el.textContent = currentYear;
  });
}

