"""Capture live GovOS UI screenshots from the running Next.js app."""
from __future__ import annotations

import json
import re
import urllib.request
from pathlib import Path

from playwright.sync_api import sync_playwright

BASE = "http://localhost:3000"
API = "http://localhost:8080"
OUT = Path(__file__).resolve().parent / "ui"
OUT.mkdir(parents=True, exist_ok=True)


def fill_login(page, username: str, password: str = "password123") -> None:
    inputs = page.locator("input").all()
    text_inputs = []
    pass_input = None
    for inp in inputs:
        t = (inp.get_attribute("type") or "text").lower()
        if t == "password":
            pass_input = inp
        elif t in ("hidden", "checkbox", "radio", "submit", "button"):
            continue
        else:
            text_inputs.append(inp)
    if text_inputs:
        text_inputs[0].fill(username)
    if pass_input:
        pass_input.fill(password)


def get_demo_totp() -> str | None:
    req = urllib.request.Request(
        f"{API}/api/auth/login",
        data=json.dumps({"username": "pm", "password": "password123"}).encode(),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=15) as r:
            data = json.loads(r.read().decode())
            for key in ("demoMfaHint", "demoTotp", "demoCode", "totp", "mfaCode", "code"):
                if data.get(key):
                    return str(data[key])
            print("login response keys:", list(data.keys()), data)
            return None
    except Exception as e:
        print("TOTP API error:", e)
        return None


def handle_mfa(page) -> None:
    page.wait_for_timeout(800)
    text = page.inner_text("body")
    lower = text.lower()
    if not any(k in lower for k in ("mfa", "totp", "6-digit", "verify", "এমএফএ", "কোড")):
        # maybe already in
        return
    code = None
    m = re.search(r"\b(\d{6})\b", text)
    if m:
        code = m.group(1)
    if not code:
        code = get_demo_totp()
    if not code:
        print("No MFA code found; body snippet:", text[:400])
        return
    print("Using MFA code:", code)
    # fill visible non-password inputs
    for inp in page.locator("input").all():
        t = (inp.get_attribute("type") or "text").lower()
        if t in ("hidden", "password", "checkbox", "radio"):
            continue
        try:
            inp.fill(str(code))
        except Exception:
            pass
    btn = page.locator("button[type='submit']")
    if btn.count() == 0:
        page.locator("form button").first.click()
    else:
        btn.first.click()
    page.wait_for_timeout(2000)


def shot(page, name: str) -> None:
    path = OUT / name
    page.screenshot(path=str(path), full_page=False)
    print("saved", path.name, "url=", page.url)


def main() -> None:
    routes = [
        ("/dashboard", "02-dashboard.png"),
        ("/projects", "03-projects.png"),
        ("/budget", "04-budget.png"),
        ("/procurement", "05-procurement.png"),
        ("/risks", "06-risks.png"),
        ("/gis", "07-gis.png"),
        ("/ai", "08-ai.png"),
        ("/ministry", "09-ministry.png"),
        ("/reports", "10-reports.png"),
        ("/citizen", "11-citizen.png"),
        ("/exports", "12-exports.png"),
        ("/hr", "13-hr.png"),
        ("/kpis", "14-kpis.png"),
        ("/documents", "15-documents.png"),
        ("/notifications", "16-notifications.png"),
        ("/field", "17-field.png"),
    ]

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(
            viewport={"width": 1440, "height": 900},
            device_scale_factor=1.25,
        )
        page = context.new_page()

        page.goto(f"{BASE}/login", wait_until="networkidle", timeout=60000)
        page.wait_for_timeout(600)
        shot(page, "01-login.png")

        fill_login(page, "pm")
        # Prefer submit button; fall back to any primary action button
        btn = page.locator("button[type='submit']")
        if btn.count() == 0:
            btn = page.locator("form button").first
        else:
            btn = btn.first
        btn.click()
        handle_mfa(page)
        page.wait_for_timeout(1500)
        print("post-login url:", page.url)

        for path, name in routes:
            try:
                page.goto(f"{BASE}{path}", wait_until="networkidle", timeout=45000)
                page.wait_for_timeout(1400)
                # wait for loading to finish if present
                try:
                    page.wait_for_selector("text=Loading", state="hidden", timeout=5000)
                except Exception:
                    pass
                shot(page, name)
            except Exception as e:
                print("FAIL", name, e)

        # Mobile field
        context2 = browser.new_context(
            viewport={"width": 390, "height": 844},
            device_scale_factor=2,
            is_mobile=True,
            has_touch=True,
        )
        p2 = context2.new_page()
        p2.goto(f"{BASE}/login", wait_until="networkidle", timeout=60000)
        fill_login(p2, "field.dhaka")
        p2.locator("button[type='submit']").first.click()
        p2.wait_for_timeout(2500)
        try:
            p2.goto(f"{BASE}/field", wait_until="networkidle", timeout=45000)
            p2.wait_for_timeout(1400)
            p2.screenshot(path=str(OUT / "18-field-mobile.png"), full_page=False)
            print("saved 18-field-mobile.png", p2.url)
        except Exception as e:
            print("field mobile fail", e)

        browser.close()

    print("Done. Files:", sorted(x.name for x in OUT.glob("*.png")))


if __name__ == "__main__":
    main()
