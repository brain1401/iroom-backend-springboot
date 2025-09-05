#!/bin/bash

echo "🧪 Testing Teacher Dashboard Endpoints After totalScore Fixes"
echo "============================================================="

BASE_URL="http://localhost:3055"

echo ""
echo "1️⃣ Testing Score Distribution Endpoint (Grade 1)..."
echo "GET $BASE_URL/teacher/dashboard/score-distribution?grade=1"
curl -s "$BASE_URL/teacher/dashboard/score-distribution?grade=1" | python3 -m json.tool

echo ""
echo "2️⃣ Testing Score Distribution Endpoint (Grade 2)..."  
echo "GET $BASE_URL/teacher/dashboard/score-distribution?grade=2"
curl -s "$BASE_URL/teacher/dashboard/score-distribution?grade=2" | python3 -m json.tool

echo ""
echo "3️⃣ Testing Exam Average Scores Endpoint (Grade 1)..."
echo "GET $BASE_URL/teacher/dashboard/exam-average-scores?grade=1&limit=5"
curl -s "$BASE_URL/teacher/dashboard/exam-average-scores?grade=1&limit=5" | python3 -m json.tool

echo ""
echo "4️⃣ Testing Recent Exams Status Endpoint (Grade 1)..."
echo "GET $BASE_URL/teacher/dashboard/recent-exams-status?grade=1&limit=5"
curl -s "$BASE_URL/teacher/dashboard/recent-exams-status?grade=1&limit=5" | python3 -m json.tool

echo ""
echo "✅ Test completed. Check the responses above for actual data instead of zeros."
echo "If you see real score values and distributions, the fix was successful!"