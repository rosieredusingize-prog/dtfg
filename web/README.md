# DTFG World Hub AI — Web Platform

**DTFG World Hub AI** is a public, multi-assistant intelligence platform featuring seven independent AI assistants powered by Google Gemini with autonomous fallback resilience.

---

## 🌟 The 7 Specialized Assistants

1. **DTFG General AI** — General knowledge, everyday questions, executive briefings, and multifaceted inquiries.
2. **DTFG Tech AI** — Software engineering, clean architecture, DevOps, cloud systems, and code debugging.
3. **DTFG Business AI** — Business planning, unit economics (CAC/LTV), fundraising, and commercial strategy.
4. **DTFG Project AI** — Agile/Scrum delivery, sprint roadmaps, WBS, milestones, and risk registers.
5. **DTFG Research AI** — Scientific inquiry, academic literature review, empirical validity, and synthesis.
6. **DTFG Data AI** — Quantitative analysis, SQL query craftsmanship, statistical interpretation, and telemetry.
7. **DTFG Learning AI** — Adaptive tutoring, intuitive analogies, Feynman masterclasses, and interactive quizzes.

---

## 🚀 Running Locally or Deploying

### Option 1: Instant Local Static Server
You can run this web app directly in any browser:
```bash
npx serve web
# or
python3 -m http.server 3000 --directory web
```
Then visit `http://localhost:3000` in your web browser.

### Option 2: 100% Free Public Hosting (No Netlify or Paid Platform Needed)
You can deploy this directory to any free static hosting provider:
- **GitHub Pages**: Push this directory to a GitHub repository, go to `Settings > Pages`, and select `main/web` branch.
- **Vercel**: Deploy with `npx vercel web` (Free tier).
- **Cloudflare Pages**: Connect your repository with static build output directory `web`.
- **Firebase Hosting**: Free tier static hosting via `firebase deploy --only hosting`.

---

## 🔒 Security & Privacy
- **Public Visitor Access**: Visitors can chat with all 7 assistants immediately without logging in.
- **No Visitor API Keys Required**: Visitors are never prompted for sensitive keys.
- **Persistent Local History**: Conversations are stored on-device in `localStorage` with full search and clear controls.
