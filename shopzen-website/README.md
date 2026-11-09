# ShopZen Official Static Website

The official product and marketing website for **ShopZen: Grocery List & Budget** (Android application `com.barakwebsoft.shopzen`), hosted at:
**[https://shopzen.barakwebsoft.com](https://shopzen.barakwebsoft.com)**

Built with semantic HTML5, modern CSS3, and vanilla JavaScript. Completely static, lightweight, zero-dependency, and ready for deployment on Hostinger shared hosting (Apache / LiteSpeed).

---

## 1. Directory Structure

```text
shopzen-website/
├── index.html                   # Official product landing page
├── about/
│   └── index.html               # Product philosophy, story, and developer info
├── support/
│   └── index.html               # FAQ, guidance, and bug reporting center
├── privacy-policy/
│   └── index.html               # Official Google Play-compliant HTML Privacy Policy
├── 404.html                     # Custom branded 404 error page
│
├── assets/
│   ├── css/
│   │   └── style.css            # Responsive Botanical design system stylesheet
│   ├── js/
│   │   └── main.js              # Vanilla JS for tabs, drawer, modal, and FAQ
│   ├── images/
│   │   ├── shopzen-logo.svg     # Official vector logo with wordmark
│   │   ├── shopzen-icon.svg     # Official vector emblem (basket + checkmark)
│   │   ├── shopzen-icon.png     # High-res 512x512 PNG icon
│   │   ├── google-play-badge.svg# Vector store badge placeholder
│   │   ├── og-cover.png         # 1200x630 Open Graph & Twitter social card
│   │   └── app-preview/
│   │       ├── preview-dashboard.svg   # Shopping list dashboard mockup
│   │       ├── preview-items.svg       # Categorized grocery items mockup
│   │       ├── preview-market-mode.svg # One-handed Market Mode HUD mockup
│   │       ├── preview-budget.svg      # Real-time budget analysis mockup
│   │       └── preview-history.svg     # Immutable historical trips mockup
│   └── favicon/
│       ├── favicon.ico          # Multi-size standard favicon
│       ├── favicon-16x16.png    # 16x16 browser tab icon
│       ├── favicon-32x32.png    # 32x32 retina browser tab icon
│       ├── apple-touch-icon.png # 180x180 iOS touch icon
│       └── site.webmanifest     # PWA / web application manifest
│
├── .htaccess                    # Hostinger Apache/LiteSpeed routing, HTTPS, cache, security
├── robots.txt                   # Search crawler directives
├── sitemap.xml                  # XML sitemap with canonical URLs
└── README.md                    # Setup, deployment, and maintenance guide
```

---

## 2. Running Locally

Because this is a pure static website, you do not need Node.js, PHP, or database servers. You can run it with any standard local HTTP server:

### Option A: Python Built-in Server (Recommended)
From the project root:
```powershell
cd shopzen-website
python -m http.server 8000
```
Open your browser at: `http://localhost:8000`

### Option B: VS Code / IDE Live Server
Right-click `shopzen-website/index.html` and select **"Open with Live Server"**.

### Option C: Node.js `npx serve`
```powershell
cd shopzen-website
npx serve .
```

---

## 3. Hostinger Shared Hosting Deployment Runbook

### Step 1: Subdomain Setup on Hostinger
1. Log in to your **Hostinger hPanel** (`hpanel.hostinger.com`).
2. Go to **Websites** &rarr; Select your primary domain (`barakwebsoft.com`) &rarr; **Manage**.
3. Under the **Domains** section, select **Subdomains**.
4. Create a new subdomain:
   - **Subdomain Name:** `shopzen`
   - **Custom folder for subdomain:** Check the box and set folder to: `public_html/shopzen` (or default `domains/barakwebsoft.com/public_html/shopzen`).
5. Click **Create**.

### Step 2: DNS Configuration
Hostinger automatically configures DNS if nameservers point to Hostinger. If using external DNS (e.g. Cloudflare or Namecheap), add this DNS record:
- **Type:** `A` or `CNAME`
- **Name / Host:** `shopzen`
- **Target / Value:** Your Hostinger server IP address (or `barakwebsoft.com`)
- **TTL:** Automatic or 3600

### Step 3: SSL Activation (HTTPS)
1. In hPanel, navigate to **Security** &rarr; **SSL**.
2. Locate `shopzen.barakwebsoft.com`.
3. Click **Install SSL** (Let's Encrypt Free SSL).
4. Verify that **Force HTTPS** is enabled.

### Step 4: Upload Website Files
You can upload files via **hPanel File Manager** or **FTP / SFTP**:
1. Open **File Manager** in hPanel.
2. Navigate to the subdomain root folder: `public_html/shopzen/`.
3. Upload all files and folders directly from `shopzen-website/`:
   - `index.html`
   - `about/`
   - `support/`
   - `privacy-policy/`
   - `404.html`
   - `assets/`
   - `.htaccess` *(make sure hidden/dot files are visible)*
   - `robots.txt`
   - `sitemap.xml`
4. Verify file permissions:
   - Folders: `755`
   - Files: `644`

---

## 4. How to Update the Google Play Download Link

When the official ShopZen Google Play listing is published and approved:

1. Obtain your official store URL, typically:
   ```text
   https://play.google.com/store/apps/details?id=com.barakwebsoft.shopzen
   ```
2. Search and replace `#download` or `role="button"` on the store badge placeholders across:
   - `index.html`
   - `about/index.html`
   - `support/index.html`
   - `privacy-policy/index.html`
   - `assets/js/main.js` (disable the notice modal and let direct click proceed to Play Store)
3. Change:
   ```html
   <a href="#download" class="store-badge-placeholder">
   ```
   To:
   ```html
   <a href="https://play.google.com/store/apps/details?id=com.barakwebsoft.shopzen" target="_blank" rel="noopener noreferrer" class="store-badge-placeholder">
   ```

---

## 5. How to Update the Privacy Policy

The Privacy Policy at `https://shopzen.barakwebsoft.com/privacy-policy/` is directly required for Google Play Console compliance:

1. Edit `shopzen-website/privacy-policy/index.html`.
2. Update the **Effective Date** pill and text at the top.
3. If new SDKs or features are added in future app versions (e.g. cloud backup in V2), disclose the data handling categories in Section 2, Section 3, and the Google Play Data Safety alignment table in Section 11.
4. Save and re-upload `privacy-policy/index.html` to `public_html/shopzen/privacy-policy/index.html`.

---

## 6. Verification and Smoke Testing Checklist

After deploying to Hostinger, verify the following:

- [ ] **Home Page:** `https://shopzen.barakwebsoft.com/` loads with 200 OK and valid SSL certificate.
- [ ] **About Page:** `https://shopzen.barakwebsoft.com/about/` loads cleanly with no broken images.
- [ ] **Support Page:** `https://shopzen.barakwebsoft.com/support/` opens and email link `mailto:connect@barakwebsoft.com` launches mail client.
- [ ] **Privacy Policy:** `https://shopzen.barakwebsoft.com/privacy-policy/` is publicly accessible without login and is standard HTML (not PDF).
- [ ] **Mobile Responsiveness:** Tested on mobile viewports (360px - 412px) with working hamburger menu.
- [ ] **Clean URLs & SSL:** `http://` automatically redirects to `https://`.
- [ ] **Sitemap & Robots:**
  - `https://shopzen.barakwebsoft.com/robots.txt` (200 OK)
  - `https://shopzen.barakwebsoft.com/sitemap.xml` (200 OK)
- [ ] **404 Handling:** Visiting `https://shopzen.barakwebsoft.com/non-existent-page` renders the custom branded 404 page.

