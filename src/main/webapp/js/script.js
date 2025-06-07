// E-Sports Betting Platform JavaScript

// Global variables
let userBalance = 0;
let selectedMatch = null;
let selectedTeam = null;

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

// Main initialization function
function initializeApp() {
    // Initialize components
    initializeNavigation();
    initializeForms();
    initializeBetting();
    initializeModals();
    initializeTooltips();
    initializeCountdowns();
    initializeCharts();

    // Start periodic updates
    startPeriodicUpdates();

    console.log('E-Sports Betting Platform initialized');
}

// Navigation functionality
function initializeNavigation() {
    const navLinks = document.querySelectorAll('.nav-link');

    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            // Remove active class from all links
            navLinks.forEach(l => l.classList.remove('active'));
            // Add active class to clicked link
            this.classList.add('active');
        });
    });

    // Mobile menu toggle
    const mobileMenuToggle = document.querySelector('.mobile-menu-toggle');
    const navMenu = document.querySelector('.navbar-nav');

    if (mobileMenuToggle && navMenu) {
        mobileMenuToggle.addEventListener('click', function() {
            navMenu.classList.toggle('show');
        });
    }
}

// Form handling
function initializeForms() {
    // Login form
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Registration form
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegistration);
    }

    // Profile update form
    const profileForm = document.getElementById('profileForm');
    if (profileForm) {
        profileForm.addEventListener('submit', handleProfileUpdate);
    }

    // Wallet forms
    const addFundsForm = document.getElementById('addFundsForm');
    if (addFundsForm) {
        addFundsForm.addEventListener('submit', handleAddFunds);
    }

    const withdrawForm = document.getElementById('withdrawForm');
    if (withdrawForm) {
        withdrawForm.addEventListener('submit', handleWithdrawFunds);
    }
}

// Betting functionality
function initializeBetting() {
    // Bet amount input listeners
    const betAmountInputs = document.querySelectorAll('.bet-amount-input');
    betAmountInputs.forEach(input => {
        input.addEventListener('input', calculatePotentialWinnings);
        input.addEventListener('blur', validateBetAmount);
    });

    // Team selection
    const teamButtons = document.querySelectorAll('.team-select-btn');
    teamButtons.forEach(button => {
        button.addEventListener('click', selectTeam);
    });

    // Place bet buttons
    const placeBetButtons = document.querySelectorAll('.place-bet-btn');
    placeBetButtons.forEach(button => {
        button.addEventListener('click', placeBet);
    });

    // Quick bet amount buttons
    const quickBetButtons = document.querySelectorAll('.quick-bet-btn');
    quickBetButtons.forEach(button => {
        button.addEventListener('click', setQuickBetAmount);
    });
}

// Handle login form submission
async function handleLogin(e) {
    e.preventDefault();

    const form = e.target;
    const formData = new FormData(form);
    const submitBtn = form.querySelector('button[type="submit"]');

    // Show loading state
    setButtonLoading(submitBtn, true);

    try {
        const response = await fetch(form.action, {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            // Login successful, redirect will be handled by server
            window.location.reload();
        } else {
            // Handle login error
            showAlert('Login failed. Please check your credentials.', 'danger');
        }
    } catch (error) {
        console.error('Login error:', error);
        showAlert('An error occurred during login. Please try again.', 'danger');
    } finally {
        setButtonLoading(submitBtn, false);
    }
}

// Handle registration form submission
async function handleRegistration(e) {
    e.preventDefault();

    const form = e.target;
    const formData = new FormData(form);
    const submitBtn = form.querySelector('button[type="submit"]');

    // Validate passwords match
    const password = formData.get('password');
    const confirmPassword = formData.get('confirmPassword');

    if (password !== confirmPassword) {
        showAlert('Passwords do not match.', 'danger');
        return;
    }

    // Validate password strength
    if (!isPasswordStrong(password)) {
        showAlert('Password must be at least 8 characters with uppercase, lowercase, number and special character.', 'danger');
        return;
    }

    setButtonLoading(submitBtn, true);

    try {
        const response = await fetch(form.action, {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            showAlert('Registration successful! Welcome to E-Sports Betting!', 'success');
            // Redirect will be handled by server
        } else {
            const errorText = await response.text();
            showAlert('Registration failed. Please try again.', 'danger');
        }
    } catch (error) {
        console.error('Registration error:', error);
        showAlert('An error occurred during registration. Please try again.', 'danger');
    } finally {
        setButtonLoading(submitBtn, false);
    }
}

// Handle team selection
function selectTeam(e) {
    const button = e.target.closest('.team-select-btn');
    const matchCard = button.closest('.match-card');
    const teamId = button.dataset.teamId;
    const teamName = button.dataset.teamName;
    const odds = button.dataset.odds;

    // Remove previous selections in this match
    matchCard.querySelectorAll('.team-select-btn').forEach(btn => {
        btn.classList.remove('selected');
    });

    // Select this team
    button.classList.add('selected');

    // Store selection
    selectedMatch = button.dataset.matchId;
    selectedTeam = teamId;

    // Update betting panel
    updateBettingPanel(teamName, odds);

    // Show betting form
    const bettingPanel = matchCard.querySelector('.betting-panel');
    if (bettingPanel) {
        bettingPanel.style.display = 'block';
        bettingPanel.scrollIntoView({ behavior: 'smooth' });
    }
}

// Update betting panel with selected team info
function updateBettingPanel(teamName, odds) {
    const selectedTeamDisplay = document.querySelector('.selected-team-display');
    const selectedOddsDisplay = document.querySelector('.selected-odds-display');

    if (selectedTeamDisplay) {
        selectedTeamDisplay.textContent = teamName;
    }

    if (selectedOddsDisplay) {
        selectedOddsDisplay.textContent = odds;
    }
}

// Calculate potential winnings
function calculatePotentialWinnings(e) {
    const betAmount = parseFloat(e.target.value) || 0;
    const matchCard = e.target.closest('.match-card');
    const selectedTeamBtn = matchCard?.querySelector('.team-select-btn.selected');

    if (!selectedTeamBtn || betAmount <= 0) {
        updateWinningsDisplay(0);
        return;
    }

    const odds = parseFloat(selectedTeamBtn.dataset.odds) || 0;
    const potentialWinnings = betAmount * odds;
    const profit = potentialWinnings - betAmount;

    updateWinningsDisplay(potentialWinnings, profit);
}

// Update winnings display
function updateWinningsDisplay(winnings, profit = 0) {
    const winningsDisplay = document.querySelector('.potential-winnings-amount');
    const profitDisplay = document.querySelector('.potential-profit-amount');

    if (winningsDisplay) {
        winningsDisplay.textContent = `${winnings.toFixed(2)}`;
    }

    if (profitDisplay) {
        profitDisplay.textContent = `Profit: ${profit.toFixed(2)}`;
    }
}

// Validate bet amount
function validateBetAmount(e) {
    const betAmount = parseFloat(e.target.value) || 0;
    const minBet = 1;
    const maxBet = 10000;

    if (betAmount < minBet) {
        showAlert(`Minimum bet amount is ${minBet}`, 'warning');
        e.target.value = minBet;
    } else if (betAmount > maxBet) {
        showAlert(`Maximum bet amount is ${maxBet}`, 'warning');
        e.target.value = maxBet;
    } else if (betAmount > userBalance) {
        showAlert('Insufficient balance for this bet amount', 'warning');
        e.target.value = Math.min(userBalance, maxBet);
    }
}

// Set quick bet amount
function setQuickBetAmount(e) {
    const amount = e.target.dataset.amount;
    const betAmountInput = e.target.closest('.betting-form').querySelector('.bet-amount-input');

    if (betAmountInput) {
        betAmountInput.value = amount;
        betAmountInput.dispatchEvent(new Event('input'));
    }
}

// Place bet
async function placeBet(e) {
    e.preventDefault();

    const button = e.target;
    const matchCard = button.closest('.match-card');
    const betAmountInput = matchCard.querySelector('.bet-amount-input');
    const betAmount = parseFloat(betAmountInput.value) || 0;

    if (!selectedMatch || !selectedTeam || betAmount <= 0) {
        showAlert('Please select a team and enter a valid bet amount', 'danger');
        return;
    }

    setButtonLoading(button, true);

    try {
        const response = await fetch('/esports-betting/bets/place', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
                matchId: selectedMatch,
                teamId: selectedTeam,
                amount: betAmount
            })
        });

        const result = await response.json();

        if (result.success) {
            showAlert(`Bet placed successfully! Potential winnings: ${result.potentialWinnings}`, 'success');

            // Update user balance
            updateUserBalance();

            // Reset betting form
            resetBettingForm(matchCard);

            // Refresh match data
            refreshMatchData(selectedMatch);

        } else {
            showAlert(result.error || 'Failed to place bet', 'danger');
        }

    } catch (error) {
        console.error('Bet placement error:', error);
        showAlert('An error occurred while placing the bet', 'danger');
    } finally {
        setButtonLoading(button, false);
    }
}

// Reset betting form
function resetBettingForm(matchCard) {
    const betAmountInput = matchCard.querySelector('.bet-amount-input');
    const teamButtons = matchCard.querySelectorAll('.team-select-btn');
    const bettingPanel = matchCard.querySelector('.betting-panel');

    if (betAmountInput) betAmountInput.value = '';

    teamButtons.forEach(btn => btn.classList.remove('selected'));

    if (bettingPanel) bettingPanel.style.display = 'none';

    updateWinningsDisplay(0);
    selectedMatch = null;
    selectedTeam = null;
}

// Update user balance
async function updateUserBalance() {
    try {
        const response = await fetch('/esports-betting/api/user/balance');
        const data = await response.json();

        if (data.success) {
            userBalance = data.balance;
            const balanceDisplays = document.querySelectorAll('.user-balance');
            balanceDisplays.forEach(display => {
                display.textContent = `${userBalance.toFixed(2)}`;
            });
        }
    } catch (error) {
        console.error('Error updating balance:', error);
    }
}

// Refresh match data
async function refreshMatchData(matchId) {
    try {
        const response = await fetch(`/esports-betting/matches/details/${matchId}?format=json`);
        const data = await response.json();

        if (data.match) {
            // Update match card with new odds and betting stats
            updateMatchCard(data.match, data.bettingStats);
        }
    } catch (error) {
        console.error('Error refreshing match data:', error);
    }
}

// Update match card with new data
function updateMatchCard(match, bettingStats) {
    const matchCard = document.querySelector(`[data-match-id="${match.id}"]`);
    if (!matchCard) return;

    // Update odds
    const team1OddsDisplay = matchCard.querySelector('.team1-odds');
    const team2OddsDisplay = matchCard.querySelector('.team2-odds');

    if (team1OddsDisplay) team1OddsDisplay.textContent = match.team1Odds;
    if (team2OddsDisplay) team2OddsDisplay.textContent = match.team2Odds;

    // Update betting statistics
    if (bettingStats) {
        const totalBetsDisplay = matchCard.querySelector('.total-bets');
        const totalAmountDisplay = matchCard.querySelector('.total-amount');

        if (totalBetsDisplay) totalBetsDisplay.textContent = bettingStats.totalBets;
        if (totalAmountDisplay) totalAmountDisplay.textContent = `${bettingStats.totalAmount}`;
    }
}

// Handle wallet operations
async function handleAddFunds(e) {
    e.preventDefault();

    const form = e.target;
    const formData = new FormData(form);
    const submitBtn = form.querySelector('button[type="submit"]');

    setButtonLoading(submitBtn, true);

    try {
        const response = await fetch(form.action, {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            showAlert('Funds added successfully!', 'success');
            updateUserBalance();
            form.reset();
            closeModal('addFundsModal');
        } else {
            showAlert('Failed to add funds. Please try again.', 'danger');
        }
    } catch (error) {
        console.error('Add funds error:', error);
        showAlert('An error occurred while adding funds.', 'danger');
    } finally {
        setButtonLoading(submitBtn, false);
    }
}

async function handleWithdrawFunds(e) {
    e.preventDefault();

    const form = e.target;
    const formData = new FormData(form);
    const submitBtn = form.querySelector('button[type="submit"]');

    setButtonLoading(submitBtn, true);

    try {
        const response = await fetch(form.action, {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            showAlert('Withdrawal processed successfully!', 'success');
            updateUserBalance();
            form.reset();
            closeModal('withdrawModal');
        } else {
            showAlert('Failed to process withdrawal. Please try again.', 'danger');
        }
    } catch (error) {
        console.error('Withdraw funds error:', error);
        showAlert('An error occurred while processing withdrawal.', 'danger');
    } finally {
        setButtonLoading(submitBtn, false);
    }
}

// Modal functionality
function initializeModals() {
    // Modal open buttons
    const modalTriggers = document.querySelectorAll('[data-modal-target]');
    modalTriggers.forEach(trigger => {
        trigger.addEventListener('click', function() {
            const modalId = this.dataset.modalTarget;
            openModal(modalId);
        });
    });

    // Modal close buttons
    const modalCloseButtons = document.querySelectorAll('.modal-close');
    modalCloseButtons.forEach(button => {
        button.addEventListener('click', function() {
            const modal = this.closest('.modal');
            if (modal) closeModal(modal.id);
        });
    });

    // Close modal on backdrop click
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeModal(modal.id);
            }
        });
    });
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';

        // Focus first input
        const firstInput = modal.querySelector('input, select, textarea');
        if (firstInput) firstInput.focus();
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

// Countdown timers
function initializeCountdowns() {
    updateCountdowns();
    setInterval(updateCountdowns, 1000);
}

function updateCountdowns() {
    const countdownElements = document.querySelectorAll('.countdown');

    countdownElements.forEach(element => {
        const endTime = new Date(element.dataset.endTime).getTime();
        const now = new Date().getTime();
        const distance = endTime - now;

        if (distance < 0) {
            element.textContent = 'Match Started';
            element.classList.add('expired');
            return;
        }

        const hours = Math.floor(distance / (1000 * 60 * 60));
        const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((distance % (1000 * 60)) / 1000);

        element.textContent = `${hours}h ${minutes}m ${seconds}s`;
    });
}

// Tooltip initialization
function initializeTooltips() {
    const tooltipElements = document.querySelectorAll('[data-tooltip]');

    tooltipElements.forEach(element => {
        element.addEventListener('mouseenter', showTooltip);
        element.addEventListener('mouseleave', hideTooltip);
    });
}

function showTooltip(e) {
    const element = e.target;
    const tooltipText = element.dataset.tooltip;

    const tooltip = document.createElement('div');
    tooltip.className = 'tooltip';
    tooltip.textContent = tooltipText;
    tooltip.id = 'active-tooltip';

    document.body.appendChild(tooltip);

    const rect = element.getBoundingClientRect();
    tooltip.style.left = rect.left + (rect.width / 2) - (tooltip.offsetWidth / 2) + 'px';
    tooltip.style.top = rect.top - tooltip.offsetHeight - 10 + 'px';
}

function hideTooltip() {
    const tooltip = document.getElementById('active-tooltip');
    if (tooltip) {
        tooltip.remove();
    }
}

// Chart initialization (for statistics)
function initializeCharts() {
    // Initialize betting history chart
    const bettingChartCanvas = document.getElementById('bettingChart');
    if (bettingChartCanvas) {
        createBettingChart(bettingChartCanvas);
    }

    // Initialize match statistics chart
    const matchStatsCanvas = document.getElementById('matchStatsChart');
    if (matchStatsCanvas) {
        createMatchStatsChart(matchStatsCanvas);
    }
}

function createBettingChart(canvas) {
    // Simple chart implementation
    const ctx = canvas.getContext('2d');
    const data = [10, 25, 15, 30, 20, 35, 40]; // Sample data

    // Basic line chart
    ctx.strokeStyle = getComputedStyle(document.documentElement).getPropertyValue('--primary-color');
    ctx.lineWidth = 3;
    ctx.beginPath();

    data.forEach((value, index) => {
        const x = (index / (data.length - 1)) * canvas.width;
        const y = canvas.height - (value / 50) * canvas.height;

        if (index === 0) {
            ctx.moveTo(x, y);
        } else {
            ctx.lineTo(x, y);
        }
    });

    ctx.stroke();
}

// Periodic updates
function startPeriodicUpdates() {
    // Update match odds every 30 seconds
    setInterval(refreshAllMatches, 30000);

    // Update user balance every 60 seconds
    setInterval(updateUserBalance, 60000);

    // Update live match statuses every 10 seconds
    setInterval(updateLiveMatches, 10000);
}

async function refreshAllMatches() {
    try {
        const response = await fetch('/esports-betting/matches/upcoming?format=json');
        const matches = await response.json();

        matches.forEach(match => {
            updateMatchCard(match);
        });
    } catch (error) {
        console.error('Error refreshing matches:', error);
    }
}

async function updateLiveMatches() {
    try {
        const response = await fetch('/esports-betting/matches/live?format=json');
        const liveMatches = await response.json();

        // Update live match indicators
        liveMatches.forEach(match => {
            const matchCard = document.querySelector(`[data-match-id="${match.id}"]`);
            if (matchCard) {
                const statusBadge = matchCard.querySelector('.match-status');
                if (statusBadge) {
                    statusBadge.textContent = 'LIVE';
                    statusBadge.className = 'match-status status-live';
                }
            }
        });
    } catch (error) {
        console.error('Error updating live matches:', error);
    }
}

// Utility functions
function setButtonLoading(button, loading) {
    if (loading) {
        button.disabled = true;
        button.dataset.originalText = button.textContent;
        button.innerHTML = '<span class="loading"></span> Loading...';
    } else {
        button.disabled = false;
        button.textContent = button.dataset.originalText || 'Submit';
    }
}

function showAlert(message, type = 'info') {
    const alertContainer = document.querySelector('.alert-container') || createAlertContainer();

    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.innerHTML = `
        <span>${message}</span>
        <button class="alert-close" onclick="this.parentElement.remove()">&times;</button>
    `;

    alertContainer.appendChild(alert);

    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (alert.parentElement) {
            alert.remove();
        }
    }, 5000);
}

function createAlertContainer() {
    const container = document.createElement('div');
    container.className = 'alert-container';
    container.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 9999;
        max-width: 400px;
    `;
    document.body.appendChild(container);
    return container;
}

function isPasswordStrong(password) {
    const minLength = 8;
    const hasUpperCase = /[A-Z]/.test(password);
    const hasLowerCase = /[a-z]/.test(password);
    const hasNumbers = /\d/.test(password);
    const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);

    return password.length >= minLength && hasUpperCase && hasLowerCase && hasNumbers && hasSpecialChar;
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    }).format(amount);
}

function formatDate(date) {
    return new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(new Date(date));
}

// Search functionality
function initializeSearch() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(handleSearch, 300));
    }
}

function handleSearch(e) {
    const query = e.target.value.toLowerCase();
    const matchCards = document.querySelectorAll('.match-card');

    matchCards.forEach(card => {
        const teamNames = card.querySelectorAll('.team-name');
        const tournamentName = card.querySelector('.tournament-name');

        let matchFound = false;

        teamNames.forEach(team => {
            if (team.textContent.toLowerCase().includes(query)) {
                matchFound = true;
            }
        });

        if (tournamentName && tournamentName.textContent.toLowerCase().includes(query)) {
            matchFound = true;
        }

        card.style.display = matchFound || query === '' ? 'block' : 'none';
    });
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Initialize search when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    initializeSearch();
});

// Export functions for global access
window.ESportsBetting = {
    selectTeam,
    placeBet,
    openModal,
    closeModal,
    showAlert,
    updateUserBalance,
    refreshMatchData
};