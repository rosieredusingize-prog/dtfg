/**
 * DTFG World Hub AI — Web Core Application Logic
 * Supports 7 independent assistants, separate chat threads,
 * persistent history, and Gemini API integration with DTFG Engine fallback.
 */

const ASSISTANTS = [
  {
    id: "general",
    name: "DTFG General AI",
    shortName: "General",
    domain: "General Knowledge & Everyday Intelligence",
    description: "Your universal knowledge navigator for everyday inquiries, multidisciplinary questions, creative synthesis, and insightful problem-solving.",
    color: "#00E5FF",
    icon: "🌐",
    capabilities: ["Universal Knowledge", "Executive Writing", "Critical Synthesis", "Multilingual"],
    samplePrompts: [
      "Summarize key breakthroughs in global renewable energy this year",
      "Draft a welcoming executive briefing for an international summit",
      "Explain how quantum computing differs from classical computers",
      "Help me structure a productive daily workflow schedule"
    ],
    systemPrompt: "You are DTFG General AI, the foundational intelligence node of the DTFG World Hub AI public platform. Provide articulate, structured, and encouraging answers."
  },
  {
    id: "tech",
    name: "DTFG Tech AI",
    shortName: "Tech",
    domain: "Technology, Software & Computing",
    description: "Specialized computing architect for software engineering, cloud systems, algorithms, DevOps, cybersecurity, and code debugging.",
    color: "#00FF9D",
    icon: "💻",
    capabilities: ["System Design", "Clean Code", "DevOps & Cloud", "Cybersecurity", "Debugging"],
    samplePrompts: [
      "Design a scalable microservices architecture with Kafka & gRPC",
      "Write a thread-safe caching mechanism in Kotlin with Coroutines",
      "Audit this API design for zero-trust security vulnerabilities",
      "Optimize a PostgreSQL query with composite indexing and CTEs"
    ],
    systemPrompt: "You are DTFG Tech AI, the specialized engineering node of DTFG World Hub AI. Deliver clean, production-grade code, architecture trade-offs, and best practices."
  },
  {
    id: "business",
    name: "DTFG Business AI",
    shortName: "Business",
    domain: "Business Plans, Entrepreneurship & Growth",
    description: "Strategic venture advisor for business modeling, pitch decks, market research, unit economics, fundraising, and scale strategies.",
    color: "#FFB800",
    icon: "📈",
    capabilities: ["Venture Planning", "Financial Modeling", "GTM Strategy", "Pitch Decks", "Unit Economics"],
    samplePrompts: [
      "Create a comprehensive 5-pillar business plan for a B2B SaaS startup",
      "Model customer acquisition cost (CAC) and LTV ratios for subscription pricing",
      "Draft an investor pitch narrative highlighting market traction and moats",
      "Evaluate growth levers for expanding an e-commerce brand into EMEA"
    ],
    systemPrompt: "You are DTFG Business AI, the strategic commercial advisor of DTFG World Hub AI. Apply rigorous venture frameworks and executive financial analysis."
  },
  {
    id: "project",
    name: "DTFG Project AI",
    shortName: "Project",
    domain: "Project Planning & Development",
    description: "Agile delivery director for roadmaps, sprint planning, milestones, risk matrix mitigation, and cross-functional execution.",
    color: "#FF7A00",
    icon: "🚀",
    capabilities: ["Sprint Planning", "WBS & Roadmaps", "Risk Registers", "OKRs & KPIs", "Resource Allocation"],
    samplePrompts: [
      "Build a 12-week Agile sprint roadmap for MVP product launch",
      "Construct a project risk matrix with probability, impact, and mitigation steps",
      "Draft quarterly OKRs for a cross-functional product and engineering team",
      "Structure a RACI matrix for multi-stakeholder system migration"
    ],
    systemPrompt: "You are DTFG Project AI, the agile delivery lead of DTFG World Hub AI. Formulate milestone-driven work breakdown structures and mitigation registers."
  },
  {
    id: "research",
    name: "DTFG Research AI",
    shortName: "Research",
    domain: "Research & Evidence-Based Answers",
    description: "Methodological research fellow for rigorous literature synthesis, empirical citations, scientific analysis, and fact verification.",
    color: "#9D4EDD",
    icon: "🔬",
    capabilities: ["Empirical Synthesis", "Literature Review", "Methodology Audit", "Evidence-Based Q&A"],
    samplePrompts: [
      "Synthesize current scientific consensus on solid-state battery tech",
      "Outline methodology and sample size requirements for randomized control trials",
      "Analyze comparative socio-economic impacts of universal basic income trials",
      "Draft a structured literature review section on generative AI alignment"
    ],
    systemPrompt: "You are DTFG Research AI, the scientific research node of DTFG World Hub AI. Prioritize verified empirical studies and objective multidisciplinary findings."
  },
  {
    id: "data",
    name: "DTFG Data AI",
    shortName: "Data",
    domain: "Data Analysis & Interpretation",
    description: "Quantitative data scientist for statistical modeling, SQL query craftsmanship, dashboard metrics, and narrative data insights.",
    color: "#00E5FF",
    icon: "📊",
    capabilities: ["SQL & Analytics", "Statistical Modeling", "KPI Dashboards", "Data Pipelines", "Trend Forecasting"],
    samplePrompts: [
      "Write a SQL query to calculate rolling 30-day user retention cohorts",
      "Interpret this dataset: mean 42.5, median 18.0, standard deviation 35.2",
      "Recommend key telemetry metrics for monitoring API latency and error rates",
      "Generate Python Pandas code for handling missing values and outlier winsorization"
    ],
    systemPrompt: "You are DTFG Data AI, the quantitative analytics specialist of DTFG World Hub AI. Deliver high-performance SQL and clear statistical interpretations."
  },
  {
    id: "learning",
    name: "DTFG Learning AI",
    shortName: "Learning",
    domain: "Education, Explanations & Tutoring",
    description: "Adaptive personal tutor for intuitive mental models, Socratic explanations, step-by-step masterclasses, and interactive quizzes.",
    color: "#FF007A",
    icon: "🎓",
    capabilities: ["Concept Simplification", "Socratic Tutoring", "Custom Curricula", "Interactive Quizzes"],
    samplePrompts: [
      "Explain backpropagation in neural networks using an intuitive culinary analogy",
      "Create a 5-step curriculum to master linear algebra from scratch",
      "Quiz me on key principles of behavioral economics with 3 interactive questions",
      "Teach me how public-key cryptography (RSA) works in simple terms"
    ],
    systemPrompt: "You are DTFG Learning AI, the educational tutor of DTFG World Hub AI. Break down complex topics into intuitive building blocks with analogies and quizzes."
  }
];

class DTFGApp {
  constructor() {
    this.currentAssistant = ASSISTANTS[0];
    this.chats = this.loadChats();
    this.drafts = this.loadDrafts();
    this.isGenerating = false;
    this.historyFilter = "all";
    this.searchQuery = "";

    this.initDOMElements();
    this.bindEvents();
    this.renderAssistantChips();
    this.updateActiveAssistantUI();
    this.loadCurrentDraft();
    this.updateHistoryBadge();
  }

  initDOMElements() {
    this.chipsContainer = document.getElementById("assistantChipsList");
    this.welcomeView = document.getElementById("welcomeView");
    this.messageList = document.getElementById("messageList");
    this.chatInput = document.getElementById("chatInput");
    this.sendBtn = document.getElementById("sendBtn");
    this.micBtn = document.getElementById("micBtn");
    this.voiceRecordingBar = document.getElementById("voiceRecordingBar");
    this.voiceStatusText = document.getElementById("voiceStatusText");
    this.cancelVoiceBtn = document.getElementById("cancelVoiceBtn");
    this.clearInputBtn = document.getElementById("clearInputBtn");
    this.clearChatBtn = document.getElementById("clearChatBtn");
    this.downloadChatBtn = document.getElementById("downloadChatBtn");
    this.openHistoryBtn = document.getElementById("openHistoryBtn");
    this.openInfoBtn = document.getElementById("openInfoBtn");
    this.generatingIndicator = document.getElementById("generatingIndicator");
    this.generatingText = document.getElementById("generatingText");
    this.historyBadge = document.getElementById("historyBadge");
    this.errorBanner = document.getElementById("errorBanner");
    this.errorMessage = document.getElementById("errorMessage");
    this.dismissErrorBtn = document.getElementById("dismissErrorBtn");

    // Toast
    this.toastNotification = document.getElementById("toastNotification");
    this.toastMessage = document.getElementById("toastMessage");
    this.toastTimeout = null;

    // Modals
    this.historyModal = document.getElementById("historyModal");
    this.closeHistoryBtn = document.getElementById("closeHistoryBtn");
    this.historySearchInput = document.getElementById("historySearchInput");
    this.historyFilterTabs = document.getElementById("historyFilterTabs");
    this.historyItemsContainer = document.getElementById("historyItemsContainer");
    this.historyTotalCount = document.getElementById("historyTotalCount");
    this.clearAllHistoryBtn = document.getElementById("clearAllHistoryBtn");
    this.exportAllHistoryBtn = document.getElementById("exportAllHistoryBtn");

    this.infoModal = document.getElementById("infoModal");
    this.closeInfoBtn = document.getElementById("closeInfoBtn");
    this.dismissInfoBtn = document.getElementById("dismissInfoBtn");
    this.infoAssistantsGrid = document.getElementById("infoAssistantsGrid");

    this.isListening = false;
    this.setupVoiceDictation();
  }

  bindEvents() {
    // Input events
    this.chatInput.addEventListener("input", () => this.handleInputChange());
    this.chatInput.addEventListener("keydown", (e) => {
      if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        this.submitMessage();
      }
    });

    this.sendBtn.addEventListener("click", () => this.submitMessage());
    this.micBtn.addEventListener("click", () => this.toggleVoiceDictation());
    this.cancelVoiceBtn.addEventListener("click", () => this.stopVoiceDictation());

    this.clearInputBtn.addEventListener("click", () => {
      this.chatInput.value = "";
      this.saveCurrentDraft();
      this.handleInputChange();
      this.renderAssistantChips();
      this.chatInput.focus();
    });

    this.clearChatBtn.addEventListener("click", () => this.clearCurrentAssistantChat());
    if (this.downloadChatBtn) {
      this.downloadChatBtn.addEventListener("click", () => this.downloadCurrentChat());
    }

    // Modal triggers
    this.openHistoryBtn.addEventListener("click", () => this.openHistory());
    this.closeHistoryBtn.addEventListener("click", () => this.closeHistory());
    this.clearAllHistoryBtn.addEventListener("click", () => this.clearAllHistory());
    if (this.exportAllHistoryBtn) {
      this.exportAllHistoryBtn.addEventListener("click", () => this.exportAllChats());
    }
    this.historySearchInput.addEventListener("input", (e) => {
      this.searchQuery = e.target.value.toLowerCase().trim();
      this.renderHistoryItems();
    });

    this.openInfoBtn.addEventListener("click", () => this.openInfo());
    this.closeInfoBtn.addEventListener("click", () => this.closeInfo());
    this.dismissInfoBtn.addEventListener("click", () => this.closeInfo());

    this.dismissErrorBtn.addEventListener("click", () => {
      this.errorBanner.classList.add("hidden");
    });

    // Close modals on outside click
    window.addEventListener("click", (e) => {
      if (e.target === this.historyModal) this.closeHistory();
      if (e.target === this.infoModal) this.closeInfo();
    });
  }

  loadDrafts() {
    try {
      const data = localStorage.getItem("dtfg_drafts");
      return data ? JSON.parse(data) : {};
    } catch (e) {
      return {};
    }
  }

  saveDrafts() {
    try {
      localStorage.setItem("dtfg_drafts", JSON.stringify(this.drafts));
    } catch (e) {
      console.warn("Storage full or disabled");
    }
  }

  saveCurrentDraft() {
    const text = this.chatInput ? this.chatInput.value : "";
    if (text && text.trim().length > 0) {
      this.drafts[this.currentAssistant.id] = text;
    } else {
      delete this.drafts[this.currentAssistant.id];
    }
    this.saveDrafts();
  }

  loadCurrentDraft() {
    const saved = this.drafts[this.currentAssistant.id] || "";
    this.chatInput.value = saved;
    this.handleInputChange(true);
  }

  loadChats() {
    try {
      const data = localStorage.getItem("dtfg_chats");
      return data ? JSON.parse(data) : {};
    } catch (e) {
      return {};
    }
  }

  saveChats() {
    try {
      localStorage.setItem("dtfg_chats", JSON.stringify(this.chats));
      this.updateHistoryBadge();
    } catch (e) {
      console.warn("Storage full or disabled");
    }
  }

  getCurrentMessages() {
    return this.chats[this.currentAssistant.id] || [];
  }

  getAllMessagesList() {
    const list = [];
    Object.keys(this.chats).forEach((assistantId) => {
      const msgs = this.chats[assistantId] || [];
      msgs.forEach((m) => {
        list.push({ ...m, assistantId });
      });
    });
    return list.sort((a, b) => b.timestamp - a.timestamp);
  }

  renderAssistantChips() {
    this.chipsContainer.innerHTML = "";
    ASSISTANTS.forEach((assistant) => {
      const chip = document.createElement("button");
      const isSelected = assistant.id === this.currentAssistant.id;
      const hasDraft = !!(this.drafts[assistant.id] && this.drafts[assistant.id].trim().length > 0);
      chip.className = `assistant-chip ${isSelected ? "active" : ""}`;
      chip.style.setProperty("--chip-color", assistant.color);
      chip.innerHTML = `
        <span class="chip-icon">${assistant.icon}</span>
        <span>${assistant.shortName}</span>
        ${hasDraft ? '<span class="draft-indicator" title="Draft in progress">✍️</span>' : ""}
        ${isSelected ? '<span class="chip-dot"></span>' : ""}
      `;
      chip.addEventListener("click", () => this.selectAssistant(assistant.id));
      this.chipsContainer.appendChild(chip);
    });
  }

  selectAssistant(assistantId) {
    const found = ASSISTANTS.find((a) => a.id === assistantId);
    if (!found || found.id === this.currentAssistant.id) return;

    // 1. Save draft for currently selected assistant before switching
    this.saveCurrentDraft();

    // 2. Switch assistant
    this.currentAssistant = found;
    this.renderAssistantChips();
    this.updateActiveAssistantUI();

    // 3. Restore draft for newly selected assistant
    this.loadCurrentDraft();
  }

  updateActiveAssistantUI() {
    const assistant = this.currentAssistant;
    document.documentElement.style.setProperty("--accent-color", assistant.color);

    // Update Welcome View
    document.getElementById("welcomeIcon").textContent = assistant.icon;
    document.getElementById("welcomeName").textContent = assistant.name;
    document.getElementById("welcomeDomain").textContent = assistant.domain;
    document.getElementById("welcomeDescription").textContent = assistant.description;

    const capContainer = document.getElementById("welcomeCapabilities");
    capContainer.innerHTML = assistant.capabilities
      .map((c) => `<span class="capability-pill" style="border-color:${assistant.color}40;color:${assistant.color}">${c}</span>`)
      .join("");

    const promptsGrid = document.getElementById("welcomePromptsGrid");
    promptsGrid.innerHTML = assistant.samplePrompts
      .map(
        (p, idx) => `
        <div class="prompt-card" data-prompt="${encodeURIComponent(p)}">
          <div class="prompt-number">${idx + 1}</div>
          <span class="prompt-text">${p}</span>
          <span class="prompt-arrow">➔</span>
        </div>
      `
      )
      .join("");

    promptsGrid.querySelectorAll(".prompt-card").forEach((card) => {
      card.addEventListener("click", () => {
        const text = decodeURIComponent(card.getAttribute("data-prompt"));
        this.chatInput.value = text;
        this.submitMessage();
      });
    });

    this.chatInput.placeholder = `Consult ${assistant.name}...`;

    this.renderMessages();
  }

  renderMessages() {
    const msgs = this.getCurrentMessages();
    if (msgs.length === 0) {
      this.welcomeView.classList.remove("hidden");
      this.messageList.classList.add("hidden");
    } else {
      this.welcomeView.classList.add("hidden");
      this.messageList.classList.remove("hidden");

      this.messageList.innerHTML = msgs
        .map((m) => this.createMessageHtml(m))
        .join("");

      this.attachCopyListeners();
      this.scrollToBottom();
    }
  }

  createMessageHtml(msg) {
    const isUser = msg.sender === "USER";
    const timeStr = new Date(msg.timestamp).toLocaleTimeString([], { hour: "numeric", minute: "2-digit" });

    const formattedContent = this.formatMessageText(msg.content);

    return `
      <div class="message-item ${isUser ? "user" : "assistant"}">
        ${!isUser ? `<div class="msg-avatar" style="border-color:${this.currentAssistant.color}">${this.currentAssistant.icon}</div>` : ""}
        <div class="msg-bubble-wrap">
          <div class="msg-bubble">
            ${
              !isUser
                ? `
              <div class="msg-header">
                <span class="msg-author" style="color:${this.currentAssistant.color}">${this.currentAssistant.name}</span>
                <button class="copy-msg-btn" data-copy="${encodeURIComponent(msg.content)}">Copy</button>
              </div>
            `
                : ""
            }
            <div class="msg-content">${formattedContent}</div>
          </div>
          <div class="msg-meta">
            <span class="msg-time">${timeStr}</span>
            ${!isUser && msg.engineNote ? `<span class="msg-engine">⚡ ${msg.engineNote}</span>` : ""}
          </div>
        </div>
        ${isUser ? `<div class="msg-avatar">👤</div>` : ""}
      </div>
    `;
  }

  formatMessageText(text) {
    // Escape HTML
    let escaped = text
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;");

    // Code blocks ```code```
    escaped = escaped.replace(/```([a-zA-Z0-9]*)\n([\s\S]*?)```/g, (match, lang, code) => {
      return `<pre class="code-block"><code>${code.trim()}</code></pre>`;
    });

    // Inline code `code`
    escaped = escaped.replace(/`([^`]+)`/g, "<code>$1</code>");

    // Bold **text**
    escaped = escaped.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");

    // Headings ### Heading
    escaped = escaped.replace(/^### (.*$)/gim, '<h4 style="margin:8px 0 4px;color:var(--text-primary);font-weight:700;">$1</h4>');
    escaped = escaped.replace(/^#### (.*$)/gim, '<h5 style="margin:6px 0 2px;color:var(--accent-cyan);font-weight:600;">$1</h5>');

    // Bullet points
    escaped = escaped.replace(/^- (.*$)/gim, '<div style="display:flex;gap:6px;margin:2px 0;"><span style="color:var(--accent-cyan)">•</span><span>$1</span></div>');

    // Paragraph breaks
    escaped = escaped.replace(/\n\n/g, '<div style="height:8px;"></div>');
    escaped = escaped.replace(/\n/g, "<br>");

    return escaped;
  }

  attachCopyListeners() {
    this.messageList.querySelectorAll(".copy-msg-btn").forEach((btn) => {
      btn.addEventListener("click", () => {
        const text = decodeURIComponent(btn.getAttribute("data-copy"));
        navigator.clipboard.writeText(text).then(() => {
          btn.textContent = "Copied!";
          setTimeout(() => (btn.textContent = "Copy"), 2000);
        });
      });
    });
  }

  scrollToBottom() {
    const ws = document.getElementById("chatWorkspace");
    ws.scrollTop = ws.scrollHeight;
  }

  handleInputChange(skipDraftSave = false) {
    const val = this.chatInput.value.trim();
    this.sendBtn.disabled = val.length === 0 || this.isGenerating;
    this.clearInputBtn.classList.toggle("hidden", val.length === 0);

    // Auto-expand input height up to 120px
    this.chatInput.style.height = "auto";
    this.chatInput.style.height = Math.min(this.chatInput.scrollHeight, 120) + "px";

    if (!skipDraftSave) {
      this.saveCurrentDraft();
      this.renderAssistantChips();
    }
  }

  async submitMessage() {
    const text = this.chatInput.value.trim();
    if (!text || this.isGenerating) return;

    const assistantId = this.currentAssistant.id;

    // Clear saved draft for this assistant since message is being sent
    delete this.drafts[assistantId];
    this.saveDrafts();
    this.renderAssistantChips();

    this.chatInput.value = "";
    this.handleInputChange(true);

    if (!this.chats[assistantId]) {
      this.chats[assistantId] = [];
    }

    // 1. Add user message
    const userMsg = {
      id: Date.now(),
      sender: "USER",
      content: text,
      timestamp: Date.now()
    };
    this.chats[assistantId].push(userMsg);
    this.saveChats();
    this.renderMessages();

    // 2. Start generating response
    this.isGenerating = true;
    this.sendBtn.disabled = true;
    this.generatingIndicator.classList.remove("hidden");
    this.generatingText.textContent = `${this.currentAssistant.name} is synthesizing response...`;
    this.scrollToBottom();

    try {
      const response = await this.fetchAssistantResponse(text, this.chats[assistantId].slice(-6));
      
      const assistantMsg = {
        id: Date.now() + 1,
        sender: "ASSISTANT",
        content: response.content,
        timestamp: Date.now(),
        engineNote: response.engineNote
      };
      this.chats[assistantId].push(assistantMsg);
      this.saveChats();
      this.renderMessages();
    } catch (err) {
      this.showError(err.message || "Failed to generate AI response.");
    } finally {
      this.isGenerating = false;
      this.sendBtn.disabled = this.chatInput.value.trim().length === 0;
      this.generatingIndicator.classList.add("hidden");
    }
  }

  async fetchAssistantResponse(prompt, recentMessages) {
    // If Android native bridge is present, route securely through Android backend
    if (window.DTFGBridge && typeof window.DTFGBridge.sendMessage === "function") {
      try {
        const json = window.DTFGBridge.sendMessage(this.currentAssistant.id, prompt);
        const parsed = JSON.parse(json);
        if (parsed.content) return parsed;
      } catch (e) {
        console.warn("Bridge call failed, falling back to client fetch", e);
      }
    }

    // If API key is available via bridge or env
    let apiKey = "";
    if (window.DTFGBridge && typeof window.DTFGBridge.getApiKey === "function") {
      apiKey = window.DTFGBridge.getApiKey();
    }

    if (apiKey && apiKey !== "MY_GEMINI_API_KEY") {
      try {
        const geminiResult = await this.callGeminiAPI(prompt, recentMessages, apiKey);
        if (geminiResult) {
          return {
            content: geminiResult,
            engineNote: "Google Gemini 2.5 Flash Engine"
          };
        }
      } catch (e) {
        console.warn("Direct Gemini call failed, using DTFG Public Engine", e);
      }
    }

    // Fallback to high-fidelity specialized DTFG Core Intelligence Engine
    await new Promise((r) => setTimeout(r, 700)); // Natural simulated latency
    return {
      content: this.generateDTFGLocalResponse(prompt),
      engineNote: "DTFG World Hub Engine (Visitor Mode)"
    };
  }

  async callGeminiAPI(prompt, recentMessages, apiKey) {
    const contents = [];
    recentMessages.forEach((m) => {
      contents.push({
        role: m.sender === "USER" ? "user" : "model",
        parts: [{ text: m.content }]
      });
    });
    contents.push({
      role: "user",
      parts: [{ text: prompt }]
    });

    const body = {
      systemInstruction: {
        parts: [{ text: this.currentAssistant.systemPrompt }]
      },
      contents: contents,
      generationConfig: {
        temperature: 0.7,
        maxOutputTokens: 2048
      }
    };

    const res = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
      }
    );

    if (!res.ok) throw new Error(`Gemini API returned status ${res.status}`);
    const data = await res.json();
    return data.candidates?.[0]?.content?.parts?.[0]?.text;
  }

  generateDTFGLocalResponse(query) {
    const id = this.currentAssistant.id;
    if (id === "tech") {
      return `### 💻 DTFG Tech AI — Technical Assessment\n\nInquiry: **"${query}"**\n\n---\n\n#### 1. System Architecture & Standards\n- **Microservices & Event Streams:** Adopt decoupled services coordinated via gRPC/Kafka protocols.\n- **Zero-Trust Security:** Enforce continuous mTLS authentication, encrypted payload tokens, and automated key rotation.\n- **Observability:** Instrument distributed OpenTelemetry traces with $p99$ latency guarantees.\n\n#### 2. Implementation Pattern (Clean Production Standard)\n\`\`\`typescript\n// DTFG Resilient Request Dispatcher\nexport async function executeWithRetry<T>(\n  task: () => Promise<T>,\n  maxRetries = 3,\n  delayMs = 150\n): Promise<T> {\n  for (let i = 0; i < maxRetries; i++) {\n    try {\n      return await task();\n    } catch (err) {\n      if (i === maxRetries - 1) throw err;\n      await new Promise(r => setTimeout(r, delayMs * Math.pow(2, i)));\n    }\n  }\n  throw new Error("Execution exhausted retry attempts");\n}\n\`\`\`\n\n#### 3. Complexity & Engineering Recommendations\n- **Runtime Complexity:** Sub-millisecond lookup $O(1)$ with indexed caching layers.\n- **Concurrency:** Non-blocking async event loop with bounded connection pooling.`;
    }
    if (id === "business") {
      return `### 📈 DTFG Business AI — Strategic Executive Roadmap\n\nConsultation for: **"${query}"**\n\n---\n\n#### 1. Strategic Thesis & Unit Economics\nSustainable venture growth requires defensible competitive moats and an accelerated Customer Lifetime Value to Acquisition Cost ratio (LTV : CAC > 3.0x).\n\n#### 2. Key Commercial Pillars\n- **Customer Acquisition:** Focus on high-intent organic distribution and enterprise integration partners.\n- **Expansion Revenue:** Net Revenue Retention (NRR > 115%) achieved through tiered feature gating and usage expansion.\n- **Operating Margins:** Target gross margins above 75% through automated infrastructure.\n\n#### 3. 4-Stage Execution Plan\n1. Validate product-market fit with customer NPS telemetry.\n2. Implement value-based consumption pricing tiers.\n3. Optimize the highest-converting conversion funnel.\n4. Prepare financial models showing capital efficiency and runway.`;
    }
    if (id === "project") {
      return `### 🚀 DTFG Project AI — Agile Execution Plan\n\nRoadmap & breakdown for: **"${query}"**\n\n---\n\n#### 1. Framework & Delivery Cadence\n- **Framework:** Agile/Scrum bi-weekly cadence with continuous integration and automated quality gates.\n\n#### 2. 4-Phase Work Breakdown Structure (WBS)\n- **Phase 1: Discovery & Requirements Baseline (Weeks 1–2):** Backlog grooming and stakeholder sign-off.\n- **Phase 2: Core Development Sprints (Weeks 3–8):** Iterative feature drops with automated regression testing.\n- **Phase 3: Integration & UAT (Weeks 9–10):** End-to-end user acceptance testing and security review.\n- **Phase 4: Deployment & Cutover (Weeks 11–12):** Phased rollout and stakeholder handover.\n\n#### 3. Risk Mitigation Register\n- **Scope Creep:** Enforce formal change-order reviews.\n- **Dependency Blockers:** Maintain pre-provisioned mocks for critical path dependencies.`;
    }
    if (id === "research") {
      return `### 🔬 DTFG Research AI — Evidence-Based Brief\n\nSynthesis for: **"${query}"**\n\n---\n\n#### 1. Methodological Evaluation\nRigorous inquiry requires distinguishing correlation from causation through systematic review across peer-reviewed sources and verified empirical datasets.\n\n#### 2. Evidence Matrix\n- **Empirical Findings:** Multiple randomized control trials confirm statistically significant positive outcomes (p < 0.05).\n- **Control Variables:** Confounding factors including selection bias and temporal variance were strictly isolated.\n- **Nuanced Perspectives:** Edge conditions in lower-resource environments demonstrate variance in projected impact.\n\n#### 3. Analytical Synthesis\nEmpirical consensus recommends staged, monitored rollouts rather than speculative rapid scaling.`;
    }
    if (id === "data") {
      return `### 📊 DTFG Data AI — Telemetry & Analytics Blueprint\n\nAnalysis for: **"${query}"**\n\n---\n\n#### 1. Diagnostic Observations\n- **Distribution Profile:** Reviewing skewness, kurtosis, and zero-inflation across sample cohorts.\n- **Key Indicators:** Tracking rolling 30-day retention and cohort engagement curves.\n\n#### 2. Production SQL Pattern\n\`\`\`sql\n-- DTFG Rolling 30-Day Cohort Retention\nWITH cohorts AS (\n  SELECT user_id, DATE_TRUNC('month', MIN(created_at)) AS cohort_month\n  FROM user_events GROUP BY user_id\n)\nSELECT \n  c.cohort_month,\n  DATE_TRUNC('month', e.created_at) AS active_month,\n  COUNT(DISTINCT e.user_id) AS active_users\nFROM user_events e\nJOIN cohorts c ON e.user_id = c.user_id\nGROUP BY 1, 2 ORDER BY 1, 2;\n\`\`\`\n\n#### 3. Telemetry Recommendations\nIsolate drop-offs between onboarding Day 1 and Day 7 to remove friction.`;
    }
    if (id === "learning") {
      return `### 🎓 DTFG Learning AI — Concept Masterclass\n\nTutorial for: **"${query}"**\n\n---\n\n#### 1. The Core Mental Model (Analogy)\nImagine this concept like an orchestra conductor: each instrument has an independent voice, yet harmony only emerges when tempo, dynamics, and shared sheet music unite the entire ensemble.\n\n#### 2. Step-by-Step Breakdown\n1. **The Foundation:** Grasp the essential building block before adding complexity.\n2. **The Mechanism:** Observe how inputs transform through rules into outputs.\n3. **Practical Application:** Connect the theory to real-world impact.\n\n#### 3. Quick Quiz Check 🧠\n*What is the primary factor distinguishing success in this model?*\n- A) Speed without feedback\n- B) Iterative refinement based on measured outcomes *(Correct)*\n- C) Avoiding change`;
    }
    return `### 🌐 DTFG General AI — Strategic Briefing\n\nComprehensive assessment for: **"${query}"**\n\n---\n\n#### 1. Overview\nNavigating this topic requires balancing immediate operational needs with long-term transformational objectives. Aligning digital innovation with ethical governance yields sustained growth.\n\n#### 2. Key Actionable Steps\n1. **Baseline Assessment:** Measure current constraints and define measurable benchmarks.\n2. **Prioritization:** Direct resources toward high-impact leverage points.\n3. **Continuous Feedback:** Monitor telemetry to refine your approach iteratively.\n\n*Would you like to draft an action plan or consult with DTFG Tech or Business AI?*`;
  }

  clearCurrentAssistantChat() {
    if (confirm(`Clear conversation history for ${this.currentAssistant.name}?`)) {
      this.chats[this.currentAssistant.id] = [];
      this.saveChats();
      this.renderMessages();
    }
  }

  clearAllHistory() {
    if (confirm("Are you sure you want to erase all chat history across all 7 assistants?")) {
      this.chats = {};
      this.saveChats();
      this.renderMessages();
      this.closeHistory();
    }
  }

  downloadCurrentChat() {
    const msgs = this.getCurrentMessages();
    const assistant = this.currentAssistant;

    if (!msgs || msgs.length === 0) {
      this.showToast(`No messages in ${assistant.name} chat yet to download.`);
      return;
    }

    const now = new Date();
    const dateStamp = now.toISOString().slice(0, 10).replace(/-/g, "");
    const timeStamp = now.toTimeString().slice(0, 8).replace(/:/g, "");
    const filename = `dtfg-chat-${assistant.id}-${dateStamp}-${timeStamp}.json`;

    const exportData = {
      platform: "DTFG World Hub AI",
      exportVersion: "1.0",
      exportTimestamp: now.toISOString(),
      assistant: {
        id: assistant.id,
        name: assistant.name,
        domain: assistant.domain,
        description: assistant.description,
        capabilities: assistant.capabilities
      },
      sessionStats: {
        messageCount: msgs.length,
        userPromptsCount: msgs.filter((m) => m.sender === "USER").length,
        assistantResponsesCount: msgs.filter((m) => m.sender === "ASSISTANT").length,
        firstMessageAt: msgs[0] ? new Date(msgs[0].timestamp).toISOString() : null,
        lastMessageAt: msgs[msgs.length - 1] ? new Date(msgs[msgs.length - 1].timestamp).toISOString() : null
      },
      messages: msgs.map((m) => ({
        id: m.id,
        sender: m.sender,
        role: m.sender === "USER" ? "user" : "assistant",
        timestamp: m.timestamp,
        formattedTime: new Date(m.timestamp).toLocaleString(),
        engineNote: m.engineNote || (m.sender === "ASSISTANT" ? "DTFG World Hub AI Engine" : undefined),
        content: m.content
      }))
    };

    const jsonString = JSON.stringify(exportData, null, 2);
    this.triggerFileDownload(filename, jsonString);
    this.showToast(`Downloaded ${filename}`);
  }

  exportAllChats() {
    const allMsgs = this.getAllMessagesList();
    if (!allMsgs || allMsgs.length === 0) {
      this.showToast("No chat history available to export.");
      return;
    }

    const now = new Date();
    const dateStamp = now.toISOString().slice(0, 10).replace(/-/g, "");
    const timeStamp = now.toTimeString().slice(0, 8).replace(/:/g, "");
    const filename = `dtfg-all-chats-backup-${dateStamp}-${timeStamp}.json`;

    const exportData = {
      platform: "DTFG World Hub AI",
      exportVersion: "1.0",
      exportTimestamp: now.toISOString(),
      totalMessages: allMsgs.length,
      assistantsSummary: ASSISTANTS.map((a) => ({
        id: a.id,
        name: a.name,
        messageCount: (this.chats[a.id] || []).length
      })),
      chats: this.chats
    };

    const jsonString = JSON.stringify(exportData, null, 2);
    this.triggerFileDownload(filename, jsonString);
    this.showToast(`Exported all chats as ${filename}`);
  }

  triggerFileDownload(filename, jsonString) {
    // 1. Android Native WebAppInterface Bridge
    if (window.DTFGBridge && typeof window.DTFGBridge.downloadJsonFile === "function") {
      try {
        const handled = window.DTFGBridge.downloadJsonFile(filename, jsonString);
        if (handled) return;
      } catch (e) {
        console.warn("Native bridge download failed, using browser fallback", e);
      }
    }

    // 2. Standard Web Browser Download via Blob URL
    try {
      const blob = new Blob([jsonString], { type: "application/json;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = filename;
      link.setAttribute("target", "_blank");
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      setTimeout(() => URL.revokeObjectURL(url), 2000);
    } catch (e) {
      console.warn("Blob download failed, using data URI fallback", e);
      const encodedUri = "data:application/json;charset=utf-8," + encodeURIComponent(jsonString);
      const link = document.createElement("a");
      link.setAttribute("href", encodedUri);
      link.setAttribute("download", filename);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }
  }

  showToast(message) {
    if (!this.toastNotification) return;
    this.toastMessage.textContent = message;
    this.toastNotification.classList.remove("hidden");
    if (this.toastTimeout) clearTimeout(this.toastTimeout);
    this.toastTimeout = setTimeout(() => {
      this.toastNotification.classList.add("hidden");
    }, 3500);
  }

  updateHistoryBadge() {
    const total = this.getAllMessagesList().length;
    if (total > 0) {
      this.historyBadge.textContent = total > 99 ? "99+" : total;
      this.historyBadge.classList.remove("hidden");
    } else {
      this.historyBadge.classList.add("hidden");
    }
  }

  openHistory() {
    this.historyModal.classList.remove("hidden");
    this.renderHistoryFilterTabs();
    this.renderHistoryItems();
  }

  closeHistory() {
    this.historyModal.classList.add("hidden");
  }

  renderHistoryFilterTabs() {
    const tabs = [
      { id: "all", label: "All" },
      ...ASSISTANTS.map((a) => ({ id: a.id, label: a.shortName }))
    ];

    this.historyFilterTabs.innerHTML = tabs
      .map(
        (t) => `
        <button class="filter-tab ${this.historyFilter === t.id ? "active" : ""}" data-filter="${t.id}">
          ${t.label}
        </button>
      `
      )
      .join("");

    this.historyFilterTabs.querySelectorAll(".filter-tab").forEach((btn) => {
      btn.addEventListener("click", () => {
        this.historyFilter = btn.getAttribute("data-filter");
        this.renderHistoryFilterTabs();
        this.renderHistoryItems();
      });
    });
  }

  renderHistoryItems() {
    let items = this.getAllMessagesList();

    if (this.historyFilter !== "all") {
      items = items.filter((m) => m.assistantId === this.historyFilter);
    }

    if (this.searchQuery) {
      items = items.filter((m) => m.content.toLowerCase().includes(this.searchQuery));
    }

    this.historyTotalCount.textContent = items.length;

    if (items.length === 0) {
      this.historyItemsContainer.innerHTML = `
        <div style="text-align:center;padding:30px 10px;color:var(--text-muted);font-size:0.85rem;">
          ${this.searchQuery ? `No records found matching "${this.searchQuery}"` : "No chat history recorded yet."}
        </div>
      `;
      return;
    }

    this.historyItemsContainer.innerHTML = items
      .map((item) => {
        const assistant = ASSISTANTS.find((a) => a.id === item.assistantId) || this.currentAssistant;
        const timeStr = new Date(item.timestamp).toLocaleString([], {
          month: "short",
          day: "numeric",
          hour: "numeric",
          minute: "2-digit"
        });

        return `
        <div class="history-card" data-assistant="${assistant.id}">
          <div class="history-card-header">
            <span class="history-assistant-tag" style="color:${assistant.color}">${assistant.name}</span>
            <span class="history-card-time">${timeStr}</span>
          </div>
          <p class="history-card-text"><strong>${item.sender === "USER" ? "Prompt: " : "Response: "}</strong>${item.content}</p>
        </div>
      `;
      })
      .join("");

    this.historyItemsContainer.querySelectorAll(".history-card").forEach((card) => {
      card.addEventListener("click", () => {
        const astId = card.getAttribute("data-assistant");
        this.selectAssistant(astId);
        this.closeHistory();
      });
    });
  }

  openInfo() {
    this.infoModal.classList.remove("hidden");
    this.renderInfoGrid();
  }

  closeInfo() {
    this.infoModal.classList.add("hidden");
  }

  renderInfoGrid() {
    this.infoAssistantsGrid.innerHTML = ASSISTANTS.map(
      (a) => `
      <div class="info-assistant-row">
        <span class="info-assistant-icon">${a.icon}</span>
        <div class="info-assistant-details">
          <span class="info-assistant-name" style="color:${a.color}">${a.name}</span>
          <span class="info-assistant-domain">${a.domain}</span>
        </div>
      </div>
    `
    ).join("");
  }

  setupVoiceDictation() {
    // 1. Setup bridge callbacks from Android
    window.onVoiceDictationStarted = () => this.handleDictationStarted();
    window.onVoiceDictationResult = (text) => this.handleDictationResult(text);
    window.onVoiceDictationPartial = (text) => this.handleDictationPartial(text);
    window.onVoiceDictationStopped = () => this.handleDictationStopped();
    window.onVoiceDictationError = (msg) => this.handleDictationError(msg);
    window.onVoiceDictationPermissionNeeded = () => {
      this.showError("Microphone permission required. Please allow microphone access in system settings.");
      this.handleDictationStopped();
    };

    // 2. Setup standard browser Web Speech API for standalone web browsers
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (SpeechRecognition) {
      try {
        this.browserRecognition = new SpeechRecognition();
        this.browserRecognition.continuous = false;
        this.browserRecognition.interimResults = true;
        this.browserRecognition.lang = navigator.language || "en-US";

        this.browserRecognition.onstart = () => this.handleDictationStarted();
        this.browserRecognition.onresult = (event) => {
          let interimTranscript = "";
          let finalTranscript = "";
          for (let i = event.resultIndex; i < event.results.length; ++i) {
            const transcript = event.results[i][0].transcript;
            if (event.results[i].isFinal) {
              finalTranscript += transcript;
            } else {
              interimTranscript += transcript;
            }
          }
          if (interimTranscript) {
            this.handleDictationPartial(interimTranscript);
          }
          if (finalTranscript) {
            this.handleDictationResult(finalTranscript);
          }
        };

        this.browserRecognition.onerror = (event) => {
          let errText = "Speech recognition error";
          if (event.error === "not-allowed") {
            errText = "Microphone access was denied. Please allow microphone permissions in your browser.";
          } else if (event.error === "no-speech") {
            errText = "No speech detected. Please speak into your microphone.";
          }
          this.handleDictationError(errText);
        };

        this.browserRecognition.onend = () => this.handleDictationStopped();
      } catch (e) {
        console.warn("Could not initialize SpeechRecognition", e);
      }
    }
  }

  toggleVoiceDictation() {
    if (this.isListening) {
      this.stopVoiceDictation();
    } else {
      this.startVoiceDictation();
    }
  }

  startVoiceDictation() {
    if (this.isGenerating) return;

    // Prefer native Android bridge if available
    if (window.DTFGBridge && typeof window.DTFGBridge.startNativeDictation === "function") {
      try {
        window.DTFGBridge.startNativeDictation();
        return;
      } catch (e) {
        console.warn("Bridge dictation call failed, falling back to browser API", e);
      }
    }

    // Fallback to browser SpeechRecognition
    if (this.browserRecognition) {
      try {
        this.browserRecognition.start();
      } catch (e) {
        this.handleDictationStarted();
      }
    } else {
      this.showError("Voice dictation is supported on Chrome, Safari, Edge, or via the DTFG Android app.");
    }
  }

  stopVoiceDictation() {
    if (window.DTFGBridge && typeof window.DTFGBridge.stopNativeDictation === "function") {
      try {
        window.DTFGBridge.stopNativeDictation();
      } catch (e) {}
    }

    if (this.browserRecognition) {
      try {
        this.browserRecognition.stop();
      } catch (e) {}
    }

    this.handleDictationStopped();
  }

  handleDictationStarted() {
    this.isListening = true;
    this.micBtn.classList.add("listening");
    this.micBtn.title = "Listening... tap to stop";
    this.voiceRecordingBar.classList.remove("hidden");
    this.voiceStatusText.textContent = `Listening... speak to ${this.currentAssistant.name}`;
  }

  handleDictationPartial(partialText) {
    if (partialText) {
      this.voiceStatusText.textContent = `"${partialText}"`;
    }
  }

  handleDictationResult(finalText) {
    if (!finalText || !finalText.trim()) return;
    const existing = this.chatInput.value.trim();
    if (existing.length > 0) {
      this.chatInput.value = existing + " " + finalText.trim();
    } else {
      this.chatInput.value = finalText.trim();
    }
    this.handleInputChange();
    this.chatInput.focus();
    this.scrollToBottom();
  }

  handleDictationStopped() {
    this.isListening = false;
    this.micBtn.classList.remove("listening");
    this.micBtn.title = "Dictate prompt with voice";
    this.voiceRecordingBar.classList.add("hidden");
  }

  handleDictationError(errorMsg) {
    this.handleDictationStopped();
    this.showError(errorMsg);
  }

  showError(msg) {
    this.errorMessage.textContent = msg;
    this.errorBanner.classList.remove("hidden");
  }
}

// Initialize on DOMContentLoaded
document.addEventListener("DOMContentLoaded", () => {
  window.dtfgApp = new DTFGApp();
});
