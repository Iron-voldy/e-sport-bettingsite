// E-Sports Betting Platform Complete JavaScript

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
    console.log('E-Sports Betting Platform initializing...');

    // Initialize components
    initializeNavigation();
    initializeForms();
    initializeBetting();
    initializeModals();
    initializeCountdowns();
    initializeSearch();

    // Start periodic updates
    startPeriodicUpdates();

    console.log('E-Sports Betting Platform initialized successfully');
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

    // Add funds form
    const addFundsForm = document.getElementById('addFundsForm');
    if (addFundsForm) {
        addFundsForm.addEventListener('submit', handleAddFunds);
    }

    // Withdraw form
    const withdrawForm = document.getElementById('withdrawForm');
    if (withdrawForm) {
        withdrawForm.addEventListener('submit', handleWithdrawFunds);
    }
}

// Betting functionality
function initializeBetting() {
    // Bet amount input listeners
    document.addEventListener('input', function(e) {
        if (e.target.classList.contains('bet-amount-input')) {
            calculatePotentialWinnings(e);
            validateBetAmount(e);
        }
    });

    // Team selection
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('team-select-btn')) {
            selectTeam(e);
        }
    });

    // Place bet buttons
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('place-bet-btn')) {
            placeBet(e);
        }
    });

    // Quick bet amount buttons
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('quick-bet-btn')) {
            setQuickBetAmount(e);
        }
    });
}

// Handle login form submission
async function handleLogin(e) {
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

        // Check if response is redirect or contains error
        const responseText = await response.text();

        if (response.ok && !responseText.includes('errorMessage')) {
            // Success - page will redirect
            window.location.reload();
        } else {
            // Extract error message if present
            const parser = new DOMParser();
            const doc = parser.parseFromString(responseText, 'text/html');
            const errorElement = doc.querySelector('.alert-danger span');
            const errorMessage = errorElement ? errorElement.textContent : 'Login failed. Please check your credentials.';

            showAlert(errorMessage, 'danger');
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

        const responseText = await response.text();

        if (response.ok && !responseText.includes('errorMessage')) {
            showAlert('Registration successful! Welcome to E-Sports Betting!', 'success');
            setTimeout(() => {
                window.location.reload();
            }, 1500);
        } else {
            // Extract error message
            const parser = new DOMParser();
            const doc = parser.parseFromString(responseText, 'text/html');
            const errorElement = doc.querySelector('.alert-danger span');
            const errorMessage = errorElement ? errorElement.textContent : 'Registration failed. Please try again.';

            showAlert(errorMessage, 'danger');
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
    if (!button) return;

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
    updateBettingPanel(matchCard, teamName, odds);

    // Show betting form
    const bettingPanel = matchCard.querySelector('.betting-panel');
    if (bettingPanel) {
        bettingPanel.style.display = 'block';
        bettingPanel.scrollIntoView({ behavior: 'smooth' });
    }
}

// Update betting panel with selected team info
function updateBettingPanel(matchCard, teamName, odds) {
    const selectedTeamDisplay = matchCard.querySelector('.selected-team-display');
    const selectedOddsDisplay = matchCard.querySelector('.selected-odds-display');

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
        updateWinningsDisplay(matchCard, 0);
        return;
    }

    const odds = parseFloat(selectedTeamBtn.dataset.odds) || 0;
    const potentialWinnings = betAmount * odds;
    const profit = potentialWinnings - betAmount;

    updateWinningsDisplay(matchCard, potentialWinnings, profit);
}

// Update winnings display
function updateWinningsDisplay(matchCard, winnings, profit = 0) {
    const winningsDisplay = matchCard?.querySelector('.potential-winnings-amount');
    const profitDisplay = matchCard?.querySelector('.potential-profit-amount');

    if (winningsDisplay) {
        winningsDisplay.textContent = `$${winnings.toFixed(2)}`;
    }

    if (profitDisplay) {
        profitDisplay.textContent = `Profit: $${profit.toFixed(2)}`;
    }
}

// Validate bet amount
function validateBetAmount(e) {
    const betAmount = parseFloat(e.target.value) || 0;
    const minBet = 1;
    const maxBet = 10000;

    if (betAmount < minBet && betAmount > 0) {
        showAlert(`Minimum bet amount is ${minBet}`, 'warning');
        e.target.value = minBet;
    } else if (betAmount > maxBet) {
        showAlert(`Maximum bet amount is ${maxBet}`, 'warning');
        e.target.value = maxBet;
    } else if (betAmount > userBalance && userBalance > 0) {
        showAlert('Insufficient balance for this bet amount', 'warning');
        e.target.value = Math.min(userBalance, maxBet);
    }
}

// Set quick bet amount
function setQuickBetAmount(e) {
    const amount = e.target.dataset.amount;
    const matchCard = e.target.closest('.match-card');
    const betAmountInput = matchCard?.querySelector('.bet-amount-input');

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
        const response = await fetch('/ESportsBetting/bets/place', {
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

    updateWinningsDisplay(matchCard, 0);
    selectedMatch = null;
    selectedTeam = null;
}

// Update user balance
async function updateUserBalance() {
    try {
        const response = await fetch('/ESportsBetting/api/user/balance');
        if (!response.ok) {
            throw new Error('Failed to fetch balance');
        }

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
        const response = await fetch(`/ESportsBetting/matches/details/${matchId}?format=json`);
        if (!response.ok) {
            throw new Error('Failed to fetch match data');
        }

        const data = await response.json();

        if (data.match) {
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
    const team1OddsDisplays = matchCard.querySelectorAll('[data-team-id="' + match.team1.id + '"]');
    const team2OddsDisplays = matchCard.querySelectorAll('[data-team-id="' + match.team2.id + '"]');

    team1OddsDisplays.forEach(display => {
        if (display.textContent !== match.team1.teamName) {
            display.textContent = match.team1Odds;
        }
    });

    team2OddsDisplays.forEach(display => {
        if (display.textContent !== match.team2.teamName) {
            display.textContent = match.team2Odds;
        }
    });
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
    document.addEventListener('click', function(e) {
        const trigger = e.target.closest('[data-modal-target]');
        if (trigger) {
            const modalId = trigger.dataset.modalTarget;
            openModal(modalId);
        }
    });

    // Modal close buttons
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('modal-close')) {
            const modal = e.target.closest('.modal');
            if (modal) closeModal(modal.id);
        }
    });

    // Close modal on backdrop click
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('modal')) {
            closeModal(e.target.id);
        }
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

        if (hours > 0) {
            element.textContent = `${hours}h ${minutes}m ${seconds}s`;
        } else if (minutes > 0) {
            element.textContent = `${minutes}m ${seconds}s`;
        } else {
            element.textContent = `${seconds}s`;
        }
    });
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
        const response = await fetch('/ESportsBetting/api/matches/upcoming');
        if (!response.ok) return;

        const data = await response.json();
        if (data.success && data.matches) {
            data.matches.forEach(match => {
                updateMatchCard(match);
            });
        }
    } catch (error) {
        console.error('Error refreshing matches:', error);
    }
}

async function updateLiveMatches() {
    try {
        const response = await fetch('/ESportsBetting/api/matches/live');
        if (!response.ok) return;

        const data = await response.json();
        if (data.success && data.matches) {
            data.matches.forEach(match => {
                const matchCard = document.querySelector(`[data-match-id="${match.id}"]`);
                if (matchCard) {
                    const statusBadge = matchCard.querySelector('.match-status');
                    if (statusBadge) {
                        statusBadge.textContent = 'LIVE';
                        statusBadge.className = 'match-status status-live';
                    }
                }
            });
        }
    } catch (error) {
        console.error('Error updating live matches:', error);
    }
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
        <button class="alert-close" onclick="this.parentElement.remove()" style="background: none; border: none; color: inherit; font-size: 1.2rem; cursor: pointer; margin-left: 1rem;">&times;</button>
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

// Cancel bet function
function cancelBet(betId) {
    if (confirm('Are you sure you want to cancel this bet?')) {
        fetch(`/ESportsBetting/bets/cancel/${betId}`, {
            method: 'POST'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAlert('Bet cancelled successfully! Refund:  + data.refundAmount, 'success');
                location.reload();
            } else {
                showAlert(data.error || 'Failed to cancel bet', 'danger');
            }
        })
        .catch(error => {
            showAlert('Error cancelling bet', 'danger');
        });
    }
}

// Export functions for global access
window.ESportsBetting = {
    selectTeam,
    placeBet,
    openModal,
    closeModal,
    showAlert,
    updateUserBalance,
    refreshMatchData,
    cancelBet
};

console.log('E-Sports Betting Platform JavaScript loaded successfully');