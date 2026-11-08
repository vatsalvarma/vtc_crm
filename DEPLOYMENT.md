# VTC CRM — Production Deployment & Subdomain Setup

This guide explains how to deploy VTC CRM on a VPS using Docker without conflicting with other running applications on the same server, and how to route traffic from a subdomain (e.g. `crm.teancoffeeindia.com`) managed on GoDaddy.

---

## Architecture Overview

```
                      Internet (Users)
                             │
                  https://crm.teancoffeeindia.com
                             ▼
                 VPS Host Nginx (Port 80/443)
                    (Existing Web Server)
                             │
                   proxy_pass http://127.0.0.1:8088
                             │
       ┌─────────────────────┼───────────────────────┐
       │ Docker Bridge Network (vtc-net)              │
       │                                             │
       │   ┌─────────────────────────────────────┐   │
       │   │  Frontend Container (Port 8088:80)  │   │
       │   │  (Nginx serving Vite SPA + Proxy)   │   │
       │   └──────────────────┬──────────────────┘   │
       │                      │ /api/                │
       │                      ▼                      │
       │   ┌─────────────────────────────────────┐   │
       │   │   Backend Container (Port 8080)     │   │
       │   │   (Spring Boot 3 + OpenJDK 17)      │   │
       │   └──────────────────┬──────────────────┘   │
       │                      │                      │
       │                      ▼                      │
       │   ┌─────────────────────────────────────┐   │
       │   │    Database Container (Port 3306)   │   │
       │   │    (MySQL 8.0 + Persistent Volume)  │   │
       │   └─────────────────────────────────────┘   │
       └─────────────────────────────────────────────┘
```

---

## Step 1: Configure GoDaddy DNS (Subdomain Setup)

Because `teancoffeeindia.com` is already serving an existing application, we create a **subdomain** for this CRM (e.g., `crm.teancoffeeindia.com` or `portal.teancoffeeindia.com`).

1. Log in to [GoDaddy Domain Portfolio](https://dcc.godaddy.com/domains).
2. Click on **teancoffeeindia.com** $\rightarrow$ Navigate to **DNS** / **Manage DNS**.
3. In the **DNS Records** table, click **Add New Record**:
   - **Type**: `A`
   - **Name**: `crm` *(or `portal` if you prefer portal.teancoffeeindia.com)*
   - **Data (Value)**: `<YOUR_VPS_PUBLIC_IP>` *(e.g. 123.45.67.89)*
   - **TTL**: `1/2 Hour` (or 600 seconds)
4. Click **Save**.

> DNS propagation usually takes between 5 to 30 minutes. You can verify it by opening terminal on your machine:
> ```bash
> ping crm.teancoffeeindia.com
> ```
> It should resolve to your VPS public IP.

---

## Step 2: VPS Host Nginx Configuration (Reverse Proxy)

On your VPS, your existing host Nginx handles incoming traffic on ports 80/443 for `teancoffeeindia.com`.
Add a separate server block configuration for the new subdomain `crm.teancoffeeindia.com`:

1. SSH into your VPS:
   ```bash
   ssh root@<YOUR_VPS_PUBLIC_IP>
   ```

2. Create a new configuration file:
   ```bash
   sudo nano /etc/nginx/sites-available/crm.teancoffeeindia.com
   ```

3. Paste the following configuration:
   ```nginx
   server {
       listen 80;
       server_name crm.teancoffeeindia.com;

       client_max_body_size 50M;

       location / {
           proxy_pass http://127.0.0.1:8088;
           proxy_http_version 1.1;
           proxy_set_header Upgrade $http_upgrade;
           proxy_set_header Connection 'upgrade';
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
           proxy_cache_bypass $http_upgrade;
       }
   }
   ```

4. Enable the site and test configuration:
   ```bash
   sudo ln -s /etc/nginx/sites-available/crm.teancoffeeindia.com /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl reload nginx
   ```

5. Install free SSL certificate using Let's Encrypt Certbot:
   ```bash
   sudo certbot --nginx -d crm.teancoffeeindia.com
   ```
   Certbot will automatically configure HTTPS on port 443 with auto-renewal.

---

## Step 3: Clone & Run VTC CRM with Docker

1. On your VPS, clone the repository:
   ```bash
   git clone https://github.com/vatsalvarma/vtc_crm.git
   cd vtc_crm
   ```

2. Create your `.env` configuration file:
   ```bash
   cp .env.example .env
   ```
   *(Optional: Edit `.env` with `nano .env` if you want a custom MySQL password or port)*

3. Start all services using Docker Compose:
   ```bash
   docker compose up -d --build
   ```

4. Verify containers are healthy and running:
   ```bash
   docker compose ps
   ```
   You should see:
   - `vtc-crm-db`: running (healthy)
   - `vtc-crm-backend`: running
   - `vtc-crm-frontend`: running on `0.0.0.0:8088->80/tcp`

5. Check logs if needed:
   ```bash
   docker compose logs -f
   ```

---

## Default Admin Credentials

Once deployed, visit `https://crm.teancoffeeindia.com`:
- **Username**: `owner`
- **Password**: `admin123`
*(Staff PIN: `4828`)*

---

## Useful Commands

- **Stop application**: `docker compose down`
- **Restart application**: `docker compose restart`
- **Rebuild after updates**: `git pull && docker compose up -d --build`
- **Backup database**:
  ```bash
  docker exec -t vtc-crm-db mysqldump -u root -p1000 vtc_crm_db > vtc_backup_$(date +%F).sql
  ```
