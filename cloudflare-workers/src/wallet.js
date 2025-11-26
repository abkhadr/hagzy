// ════════════════════════════════════════════════════════════
// 💰 Wallet Operations
// ════════════════════════════════════════════════════════════

import { getConfig, jsonResponse, errorResponse } from './config.js';

export async function processBookingPayment(request, env) {
  try {
    const config = getConfig(env);
    const body = await request.json();

    const { userId, amount, bookingId, bookingTitle } = body;

    // Validation
    if (!userId || !amount || !bookingId) {
      return errorResponse('البيانات غير كاملة');
    }

    if (amount <= 0) {
      return errorResponse('مبلغ غير صحيح');
    }

    const accessToken = env.FIREBASE_DATABASE_SECRET;

    // Get current balance
    const balanceUrl = `${config.firebase.databaseUrl}/wallets/${userId}/balance.json?auth=${accessToken}`;
    const balanceResponse = await fetch(balanceUrl);
    const currentBalance = await balanceResponse.json() || 0;

    // Check sufficient balance
    if (currentBalance < amount) {
      return errorResponse('الرصيد غير كافٍ');
    }

    // Deduct amount
    const newBalance = currentBalance - amount;
    await fetch(balanceUrl, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newBalance)
    });

    // Update total withdrawals
    const withdrawalsUrl = `${config.firebase.databaseUrl}/wallets/${userId}/totalWithdrawals.json?auth=${accessToken}`;
    const withdrawalsResponse = await fetch(withdrawalsUrl);
    const currentWithdrawals = await withdrawalsResponse.json() || 0;

    await fetch(withdrawalsUrl, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(currentWithdrawals + amount)
    });

    // Add transaction
    const transactionsUrl = `${config.firebase.databaseUrl}/wallets/${userId}/transactions.json?auth=${accessToken}`;
    await fetch(transactionsUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        type: 'booking_payment',
        amount: amount,
        status: 'completed',
        bookingId: bookingId,
        title: bookingTitle || 'دفع حجز',
        timestamp: Date.now()
      })
    });

    return jsonResponse({
      success: true,
      newBalance: newBalance,
      message: 'تم الدفع بنجاح'
    });

  } catch (error) {
    console.error('Booking Payment Error:', error);
    return errorResponse('فشل معالجة الدفع: ' + error.message, 500);
  }
}

export async function getWalletBalance(request, env) {
  try {
    const url = new URL(request.url);
    const userId = url.searchParams.get('userId');

    if (!userId) {
      return errorResponse('userId مطلوب');
    }

    const config = getConfig(env);
    const accessToken = env.FIREBASE_DATABASE_SECRET;

    const balanceUrl = `${config.firebase.databaseUrl}/wallets/${userId}.json?auth=${accessToken}`;
    const response = await fetch(balanceUrl);
    const wallet = await response.json();

    return jsonResponse({
      balance: wallet?.balance || 0,
      totalDeposits: wallet?.totalDeposits || 0,
      totalWithdrawals: wallet?.totalWithdrawals || 0
    });

  } catch (error) {
    console.error('Get Balance Error:', error);
    return errorResponse('فشل جلب الرصيد', 500);
  }
}