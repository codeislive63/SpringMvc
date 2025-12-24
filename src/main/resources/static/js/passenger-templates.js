(() => {
    const STORAGE_KEY = "rb-passenger-templates"; // оставляем тот же ключ, чтобы не потерять старые данные

    function safeJsonParse(raw, fallback) {
        try { return raw ? JSON.parse(raw) : fallback; } catch { return fallback; }
    }

    function loadRawList() {
        return safeJsonParse(localStorage.getItem(STORAGE_KEY), []);
    }

    function saveRawList(list) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
    }

    // ---- Legacy -> v2 migration ----
    // legacy: { id, name, document, benefit, loyalty, childTicket }
    // v2:     { id, lastName, firstName, middleName, gender, birthDate, docType, docNumber, benefitType, loyaltyNumber, childTicket }
    function splitFullName(name) {
        const parts = String(name || "").trim().split(/\s+/).filter(Boolean);
        return {
            lastName: parts[0] || "",
            firstName: parts[1] || "",
            middleName: parts.slice(2).join(" ") || "",
        };
    }

    function splitDocument(doc) {
        const s = String(doc || "").trim();
        if (!s) return { docType: "", docNumber: "" };

        const parts = s.split(/\s+/);
        if (parts.length === 1) return { docType: "", docNumber: s };

        return { docType: parts[0], docNumber: parts.slice(1).join(" ") };
    }

    function normalizePassenger(p) {
        const hasV2 =
            p && (("lastName" in p) || ("firstName" in p) || ("docType" in p) || ("docNumber" in p));

        if (hasV2) {
            return {
                id: p.id || (crypto.randomUUID ? crypto.randomUUID() : String(Date.now())),
                lastName: p.lastName || "",
                firstName: p.firstName || "",
                middleName: p.middleName || "",
                gender: p.gender || "",
                birthDate: p.birthDate || "",
                docType: p.docType || "",
                docNumber: p.docNumber || "",
                benefitType: p.benefitType || "",
                loyaltyNumber: p.loyaltyNumber || "",
                childTicket: !!p.childTicket,
            };
        }

        // legacy
        const id = p?.id || (crypto.randomUUID ? crypto.randomUUID() : String(Date.now()));
        const name = p?.name || "";
        const document = p?.document || "";
        const n = splitFullName(name);
        const d = splitDocument(document);

        return {
            id,
            lastName: n.lastName,
            firstName: n.firstName,
            middleName: n.middleName,
            gender: "",
            birthDate: "",
            docType: d.docType,
            docNumber: d.docNumber,
            benefitType: p?.benefit || "",
            loyaltyNumber: p?.loyalty || "",
            childTicket: !!p?.childTicket,
        };
    }

    function load() {
        const raw = loadRawList();
        const normalized = raw.map(normalizePassenger);

        const changed =
            raw.length !== normalized.length ||
            raw.some((p, i) => JSON.stringify(p) !== JSON.stringify(normalized[i]));

        if (changed) saveRawList(normalized);

        return normalized;
    }

    function displayName(p) {
        const full = [p.lastName, p.firstName, p.middleName].map(s => String(s || "").trim()).filter(Boolean).join(" ");
        return full || "Пассажир";
    }

    function documentLine(p) {
        const t = String(p.docType || "").trim();
        const n = String(p.docNumber || "").trim();
        if (t && n) return `${t} ${n}`;
        return n || "";
    }

    function upsert(passenger) {
        const p = normalizePassenger(passenger);
        const list = load();
        const idx = list.findIndex(x => x.id === p.id);
        if (idx >= 0) list[idx] = p;
        else list.push(p);
        saveRawList(list);
        return p;
    }

    function remove(id) {
        const list = load().filter(p => p.id !== id);
        saveRawList(list);
    }

    function getById(id) {
        return load().find(p => p.id === id) || null;
    }

    function fillSelect(selectEl, selectedId) {
        if (!selectEl) return;
        const list = load();
        selectEl.innerHTML = `<option value="">Новый пассажир</option>`;
        for (const p of list) {
            const opt = document.createElement("option");
            opt.value = p.id;
            opt.textContent = displayName(p);
            selectEl.appendChild(opt);
        }
        if (selectedId) selectEl.value = selectedId;
    }

    window.PassengerTemplates = {
        STORAGE_KEY,
        load,
        upsert,
        remove,
        getById,
        fillSelect,
        displayName,
        documentLine,
    };
})();
