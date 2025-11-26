// ════════════════════════════════════════════════════════════
// 💳 Paymob Integration (Improved Error Handling)
// ════════════════════════════════════════════════════════════

import { getConfig, jsonResponse, errorResponse } from './config.js';

export async function createPaymobPayment(request, env) {
  try {
    const config = getConfig(env);
    const body = await request.json();

    const { amount, userId, userEmail, userName, userPhone } = body;

    console.log('📦 Payment request:', { amount, userId, userEmail });

    // Validation
    if (!amount || amount < 10) {
      return errorResponse('المبلغ يجب أن يكون 10 ج.م على الأقل');
    }

    if (!userId) {
      return errorResponse('userId مطلوب');
    }

    // 1️⃣ Get Authentication Token
    console.log('🔐 Getting auth token...');
    const authResponse = await fetch(`${config.paymob.baseUrl}/auth/tokens`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        api_key: config.paymob.apiKey
      })
    });

    console.log('Auth response status:', authResponse.status);

    if (!authResponse.ok) {
      const errorText = await authResponse.text();
      console.error('Auth error response:', errorText);
      throw new Error(`فشل الحصول على Authentication Token: ${authResponse.status}`);
    }

    const authText = await authResponse.text();
    console.log('Auth response:', authText.substring(0, 200));

    let authData;
    try {
      authData = JSON.parse(authText);
    } catch (e) {
      console.error('Failed to parse auth response:', authText);
      throw new Error('استجابة غير صالحة من خادم الدفع (Auth)');
    }

    const authToken = authData.token;
    if (!authToken) {
      throw new Error('لم يتم استلام Authentication Token');
    }

    // 2️⃣ Create Order
    console.log('📝 Creating order...');
    const orderPayload = {
      auth_token: authToken,
      delivery_needed: 'false',
      amount_cents: Math.round(amount * 100),
      currency: 'EGP',
      merchant_order_id: `order_${Date.now()}`,
      items: [{
        name: 'Wallet Deposit',
        amount_cents: Math.round(amount * 100),
        description: 'إيداع في المحفظة',
        quantity: 1
      }]
    };

    const orderResponse = await fetch(`${config.paymob.baseUrl}/ecommerce/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(orderPayload)
    });

    console.log('Order response status:', orderResponse.status);

    if (!orderResponse.ok) {
      const errorText = await orderResponse.text();
      console.error('Order error response:', errorText);
      throw new Error(`فشل إنشاء Order: ${orderResponse.status}`);
    }

    const orderText = await orderResponse.text();
    console.log('Order response:', orderText.substring(0, 200));

    let orderData;
    try {
      orderData = JSON.parse(orderText);
    } catch (e) {
      console.error('Failed to parse order response:', orderText);
      throw new Error('استجابة غير صالحة من خادم الدفع (Order)');
    }

    const orderId = orderData.id;
    if (!orderId) {
      throw new Error('لم يتم استلام Order ID');
    }

    // 3️⃣ Get Payment Key
    console.log('🔑 Getting payment key...');
    const paymentKeyPayload = {
      auth_token: authToken,
      amount_cents: Math.round(amount * 100),
      expiration: 3600,
      order_id: orderId,
      billing_data: {
        apartment: 'NA',
        email: userEmail || 'user@example.com',
        floor: 'NA',
        first_name: userName || 'User',
        street: 'NA',
        building: 'NA',
        phone_number: userPhone || '01000000000',
        shipping_method: 'NA',
        postal_code: 'NA',
        city: 'Cairo',
        country: 'EG',
        last_name: 'User',
        state: 'Cairo'
      },
      currency: 'EGP',
      integration_id: config.paymob.integrationId,
      lock_order_when_paid: 'true',
      merchant_order_id: userId
    };

    const paymentKeyResponse = await fetch(`${config.paymob.baseUrl}/acceptance/payment_keys`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(paymentKeyPayload)
    });

    console.log('Payment key response status:', paymentKeyResponse.status);

    if (!paymentKeyResponse.ok) {
      const errorText = await paymentKeyResponse.text();
      console.error('Payment key error response:', errorText);
      throw new Error(`فشل الحصول على Payment Key: ${paymentKeyResponse.status}`);
    }

    const paymentKeyText = await paymentKeyResponse.text();
    console.log('Payment key response:', paymentKeyText.substring(0, 200));

    let paymentKeyData;
    try {
      paymentKeyData = JSON.parse(paymentKeyText);
    } catch (e) {
      console.error('Failed to parse payment key response:', paymentKeyText);
      throw new Error('استجابة غير صالحة من خادم الدفع (Payment Key)');
    }

    const paymentToken = paymentKeyData.token;
    if (!paymentToken) {
      throw new Error('لم يتم استلام Payment Token');
    }

    // 4️⃣ Create Payment URL
    const paymentUrl = `https://accept.paymob.com/api/acceptance/iframes/${config.paymob.iframeId}?payment_token=${paymentToken}`;

    // 5️⃣ Generate Transaction ID
    const transactionId = `txn_${Date.now()}_${userId.substring(0, 8)}`;

    console.log('✅ Payment created successfully');

    return jsonResponse({
      success: true,
      payment_url: paymentUrl,
      transaction_id: transactionId,
      order_id: orderId,
      amount: amount
    });

  } catch (error) {
    console.error('❌ Paymob Error:', error.message);
    console.error('Stack:', error.stack);
    return errorResponse('فشل إنشاء رابط الدفع: ' + error.message, 500);
  }
}

// ════════════════════════════════════════════════════════════
// 🔔 Paymob Webhook Handler
// ════════════════════════════════════════════════════════════

export async function handlePaymobWebhook(request, env) {
  try {
    const body = await request.json();
    console.log('📨 Paymob Webhook received:', JSON.stringify(body).substring(0, 500));

    const { obj } = body;

    if (!obj) {
      console.error('❌ Invalid webhook payload - no obj field');
      return errorResponse('Invalid webhook payload');
    }

    const {
      success,
      amount_cents,
      order,
      pending,
      merchant_order_id
    } = obj;

    const userId = merchant_order_id;
    const orderId = order?.id;

    console.log('Webhook data:', { success, pending, userId, orderId, amount_cents });

    if (!userId || !orderId) {
      console.error('❌ Missing userId or orderId');
      return jsonResponse({ status: 'ignored' });
    }

    // ✅ Payment succeeded
    if (success && !pending) {
      const amount = amount_cents / 100;
      console.log(`💰 Processing successful payment: ${amount} EGP for user ${userId}`);

      await updateWalletBalance(env, userId, amount, orderId);

      console.log(`✅ Deposit successful: ${amount} EGP for user ${userId}`);
    } else if (!success) {
      console.log(`❌ Payment failed for order ${orderId}`);
      await updateTransactionStatus(env, userId, orderId, 'failed');
    } else if (pending) {
      console.log(`⏳ Payment pending for order ${orderId}`);
    }

    return jsonResponse({ status: 'ok' });

  } catch (error) {
    console.error('❌ Webhook Error:', error.message);
    console.error('Stack:', error.stack);
    return errorResponse('Webhook processing failed', 500);
  }
}

// ════════════════════════════════════════════════════════════
// 🔧 Helper Functions
// ════════════════════════════════════════════════════════════

async function updateWalletBalance(env, userId, amount, orderId) {
  try {
    const config = getConfig(env);
    const serviceAccount = config.firebase.serviceAccount;

    // Get Firebase access token
    const accessToken = await getFirebaseAccessToken(serviceAccount, env);

    // Get current balance
    const balanceUrl = `${config.firebase.databaseUrl}/wallets/${userId}/balance.json?access_token=${accessToken}`;
    const balanceResponse = await fetch(balanceUrl);
    const currentBalance = await balanceResponse.json() || 0;

    // Update balance
    const newBalance = currentBalance + amount;
    await fetch(balanceUrl, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newBalance)
    });

    console.log(`💵 Updated balance: ${currentBalance} → ${newBalance}`);

    // Update total deposits
    const depositsUrl = `${config.firebase.databaseUrl}/wallets/${userId}/totalDeposits.json?access_token=${accessToken}`;
    const depositsResponse = await fetch(depositsUrl);
    const currentDeposits = await depositsResponse.json() || 0;

    await fetch(depositsUrl, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(currentDeposits + amount)
    });

    // Add completed transaction
    const transactionsUrl = `${config.firebase.databaseUrl}/wallets/${userId}/transactions.json?access_token=${accessToken}`;
    await fetch(transactionsUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        type: 'deposit',
        amount: amount,
        status: 'completed',
        transactionId: `txn_${orderId}`,
        timestamp: Date.now(),
        title: 'إيداع في المحفظة',
        completedAt: Date.now()
      })
    });

    console.log('✅ Transaction recorded');
  } catch (error) {
    console.error('❌ Failed to update wallet:', error.message);
    throw error;
  }
}

async function updateTransactionStatus(env, userId, orderId, status) {
  try {
    const config = getConfig(env);
    const accessToken = await getFirebaseAccessToken(config.firebase.serviceAccount, env);

    const transactionsUrl = `${config.firebase.databaseUrl}/wallets/${userId}/transactions.json?access_token=${accessToken}&orderBy="transactionId"&equalTo="txn_${orderId}"`;
    const response = await fetch(transactionsUrl);
    const transactions = await response.json();

    if (transactions) {
      for (const [key, transaction] of Object.entries(transactions)) {
        const updateUrl = `${config.firebase.databaseUrl}/wallets/${userId}/transactions/${key}/status.json?access_token=${accessToken}`;
        await fetch(updateUrl, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(status)
        });
      }
      console.log(`✅ Transaction status updated to: ${status}`);
    }
  } catch (error) {
    console.error('❌ Failed to update transaction status:', error.message);
  }
}

async function getFirebaseAccessToken(serviceAccount, env) {
  // Use Firebase Database Secret for now
  // For production, implement proper JWT signing
  return env.FIREBASE_DATABASE_SECRET || 'YOUR_DATABASE_SECRET';
}