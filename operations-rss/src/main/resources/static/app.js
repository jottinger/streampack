let currentPage = 0;
let totalPages = 0;
let currentFeedId = null;

async function loadEntries(feedId, feed = null, page = 0) {
    try {
        currentFeedId = feedId; // Track the current feed
        const response = await fetch(
            `${API_BASE_URL}${feedId ? `/${feedId}/entries` : "/entries"}?page=${page}&pageSize=10`
        );
        const data = await response.json();

        // Update pagination info
        currentPage = page;
        totalPages = data.totalPages;

        // Enable or disable pagination buttons
        document.getElementById("prev-page").disabled = currentPage === 0;
        document.getElementById("next-page").disabled = currentPage >= totalPages - 1;
        document.getElementById("page-info").textContent = `Page ${currentPage + 1} of ${totalPages}`;

        // Update feed information
        const feedInfo = document.getElementById("feed-info");
        feedInfo.innerHTML = feed
            ? `<h3>${feed.title}</h3><p><a href="${feed.url}" target="_blank">${feed.url}</a></p>`
            : "<h3>All Feeds</h3>";

        // Populate entries table
        const tbody = document.getElementById("entry-table").querySelector("tbody");
        tbody.innerHTML = ""; // Clear previous entries

        data.content.forEach(entry => {
            const row = document.createElement("tr");

            const titleCell = document.createElement("td");
            const titleLink = document.createElement("a");
            titleLink.href = entry.url;
            titleLink.textContent = entry.title;
            titleLink.target = "_blank";
            titleCell.appendChild(titleLink);

            const summaryCell = document.createElement("td");
            summaryCell.innerHTML = `
                <strong>User Summary:</strong> ${entry.summary || "N/A"}<br>
                <strong>AI Summary:</strong> ${entry.llmSummary || "N/A"}
            `;

            row.appendChild(titleCell);
            row.appendChild(summaryCell);
            tbody.appendChild(row);
        });
    } catch (error) {
        console.error("Error loading entries:", error);
    }
}

function changePage(direction) {
    const newPage = currentPage + direction;
    if (newPage >= 0 && newPage < totalPages) {
        loadEntries(currentFeedId, null, newPage);
    }
}
