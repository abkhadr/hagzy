// ════════════════════════════════════════════════════════════
// 🌐 Main Router
// ════════════════════════════════════════════════════════════

import { corsHeaders, jsonResponse, errorResponse } from './config.js';
import { createPaymobPayment, handlePaymobWebhook } from './paymob.js';
import { processBookingPayment, getWalletBalance } from './wallet.js';

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    const path = url.pathname;

    // Handle CORS preflight
    if (request.method === 'OPTIONS') {
      return new Response(null, {
        headers: corsHeaders
      });
    }

    try {
      // ══════════════════════════════════════
      // 📍 Routes
      // ══════════════════════════════════════

      // 💳 Create Paymob Payment
      if (path === '/createPaymobPayment' && request.method === 'POST') {
        return await createPaymobPayment(request, env);
      }

      // 🔔 Paymob Webhook
      if (path === '/paymobWebhook' && request.method === 'POST') {
        return await handlePaymobWebhook(request, env);
      }

      // 💰 Process Booking Payment
      if (path === '/processBookingPayment' && request.method === 'POST') {
        return await processBookingPayment(request, env);
      }

      // 📊 Get Wallet Balance
      if (path === '/getWalletBalance' && request.method === 'GET') {
        return await getWalletBalance(request, env);
      }

      // 🏠 Home
      if (path === '/' || path === '') {
        return jsonResponse({
          service: 'Hagzy Wallet API',
          version: '1.0.0',
          status: 'running',
          endpoints: [
            'POST /createPaymobPayment',
            'POST /paymobWebhook',
            'POST /processBookingPayment',
            'GET /getWalletBalance'
          ]
        });
      }

      // 404
      return errorResponse('Endpoint not found', 404);

    } catch (error) {
      console.error('Server Error:', error);
      return errorResponse('Internal server error: ' + error.message, 500);
    }
  }
};