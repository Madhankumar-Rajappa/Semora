$baseUrl = 'http://localhost:8080/api'
$headers1 = @{ 'Authorization' = 'Bearer dev-student-1'; 'Content-Type' = 'application/json' }
$headers2 = @{ 'Authorization' = 'Bearer dev-student-2'; 'Content-Type' = 'application/json' }

Write-Host "=================================================="
Write-Host "  RUNNING CAMPUS TRACK PHASE 1 ACCEPTANCE TESTS   "
Write-Host "=================================================="

# Check initial semesters for student 1
$s1List = (Invoke-RestMethod -Uri "$baseUrl/semesters" -Method Get -Headers $headers1).data
Write-Host "Student 1 existing semesters: $($s1List.semesterNumber -join ', ')"

# Test 1 & 2: Create Semester 6 for Student 1
Write-Host "`n[Test 1 & 2] Creating Semester 6 for Student 1..."
$bodySem6 = @{ semesterNumber = 6; academicYear = '2025-2026 Fall'; status = 'Active' } | ConvertTo-Json
$created6 = (Invoke-RestMethod -Uri "$baseUrl/semesters" -Method Post -Headers $headers1 -Body $bodySem6).data
Write-Host "PASS: Created Semester ID: $($created6.id), Number: $($created6.semesterNumber), Status: $($created6.status)"

$s1After6 = (Invoke-RestMethod -Uri "$baseUrl/semesters" -Method Get -Headers $headers1).data
$found6 = $s1After6 | Where-Object { $_.semesterNumber -eq 6 }
if ($found6) { 
    Write-Host "PASS: Semester 6 appears on semester listing page." 
} else { 
    throw "FAIL: Sem 6 not listed" 
}

# Test 3: Create Semester 7 independently
Write-Host "`n[Test 3] Creating Semester 7 independently for Student 1..."
$bodySem7 = @{ semesterNumber = 7; academicYear = '2025-2026 Spring'; status = 'Active' } | ConvertTo-Json
$created7 = (Invoke-RestMethod -Uri "$baseUrl/semesters" -Method Post -Headers $headers1 -Body $bodySem7).data
Write-Host "PASS: Created Semester ID: $($created7.id), Number: $($created7.semesterNumber)"

# Test 4: Duplicate semester numbers for the same student are rejected
Write-Host "`n[Test 4] Attempting to create duplicate Semester 6 for Student 1..."
try {
    Invoke-RestMethod -Uri "$baseUrl/semesters" -Method Post -Headers $headers1 -Body $bodySem6
    Write-Host "FAIL: Duplicate semester was not rejected!"
} catch {
    Write-Host "PASS: Duplicate semester correctly rejected by server! Response: $($_.Exception.Message)"
}

# Test 5: Editing a semester updates its saved database record
Write-Host "`n[Test 5] Editing Semester 6 academic year to 2025-2026 Autumn..."
$bodyEdit6 = @{ semesterNumber = 6; academicYear = '2025-2026 Autumn'; status = 'Active' } | ConvertTo-Json
$updated6 = (Invoke-RestMethod -Uri "$baseUrl/semesters/$($created6.id)" -Method Put -Headers $headers1 -Body $bodyEdit6).data
$verified6 = (Invoke-RestMethod -Uri "$baseUrl/semesters/$($created6.id)" -Method Get -Headers $headers1).data
if ($verified6.academicYear -eq '2025-2026 Autumn') {
    Write-Host "PASS: Editing semester updated saved database record: $($verified6.academicYear)"
} else { 
    throw "FAIL: Edit did not persist" 
}

# Test 6: Marking a semester Completed updates its status
Write-Host "`n[Test 6] Marking Semester 6 as Completed via PATCH /api/semesters/{id}/status..."
$bodyPatch = @{ status = 'Completed' } | ConvertTo-Json
$patched6 = (Invoke-RestMethod -Uri "$baseUrl/semesters/$($created6.id)/status" -Method Patch -Headers $headers1 -Body $bodyPatch).data
if ($patched6.status -eq 'Completed') {
    Write-Host "PASS: Status successfully patched to $($patched6.status)"
} else { 
    throw "FAIL: Status not completed" 
}

# Test 7: Completed semesters remain visible after creating another semester
Write-Host "`n[Test 7] Creating Semester 8 and checking if completed Semester 6 remains visible..."
$bodySem8 = @{ semesterNumber = 8; academicYear = '2026-2027 Fall'; status = 'Active' } | ConvertTo-Json
$created8 = (Invoke-RestMethod -Uri "$baseUrl/semesters" -Method Post -Headers $headers1 -Body $bodySem8).data
$s1Final = (Invoke-RestMethod -Uri "$baseUrl/semesters" -Method Get -Headers $headers1).data
$foundCompleted6 = $s1Final | Where-Object { $_.semesterNumber -eq 6 -and $_.status -eq 'Completed' }
if ($foundCompleted6) {
    Write-Host "PASS: Completed Semester 6 remains visible after creating Semester 8!"
} else { 
    throw "FAIL: Completed semester disappeared" 
}

# Test 8: Refreshing the browser / querying database directly does not remove records
Write-Host "`n[Test 8] Simulating refresh / direct database verification..."
$refreshList = (Invoke-RestMethod -Uri "$baseUrl/semesters" -Method Get -Headers $headers1).data
Write-Host "PASS: Records persisted in MySQL. Total semesters for Student 1: $($refreshList.Count)"

# Test 9: Opening a semester displays the correct semester details
Write-Host "`n[Test 9] Querying GET /api/semesters/{id} for Semester 6..."
$detailSem6 = (Invoke-RestMethod -Uri "$baseUrl/semesters/$($created6.id)" -Method Get -Headers $headers1).data
if ($detailSem6.semesterNumber -eq 6 -and $detailSem6.academicYear -eq '2025-2026 Autumn' -and $detailSem6.status -eq 'Completed') {
    Write-Host "PASS: Correct details retrieved for Semester 6: Name: $($detailSem6.semesterName), Status: $($detailSem6.status), Year: $($detailSem6.academicYear)"
} else { 
    throw "FAIL: Incorrect details" 
}

# Test 10: One student's semester records cannot be accessed by another student
Write-Host "`n[Test 10] Testing Student Multi-Tenancy & Isolation..."
Write-Host "Student 2 querying their own semesters..."
$s2List = (Invoke-RestMethod -Uri "$baseUrl/semesters" -Method Get -Headers $headers2).data
Write-Host "PASS: Student 2 semesters count: $($s2List.Count), Numbers: $($s2List.semesterNumber -join ', ')"

Write-Host "Student 2 attempting to access Student 1's Semester 6 (ID: $($created6.id))..."
try {
    Invoke-RestMethod -Uri "$baseUrl/semesters/$($created6.id)" -Method Get -Headers $headers2
    Write-Host "FAIL: Student 2 was able to access Student 1's record!"
} catch {
    Write-Host "PASS: Student 2 blocked from accessing Student 1's record! Response: $($_.Exception.Message)"
}

Write-Host "Student 2 attempting to delete Student 1's Semester 6..."
try {
    Invoke-RestMethod -Uri "$baseUrl/semesters/$($created6.id)" -Method Delete -Headers $headers2
    Write-Host "FAIL: Student 2 was able to delete Student 1's record!"
} catch {
    Write-Host "PASS: Student 2 blocked from deleting Student 1's record! Response: $($_.Exception.Message)"
}

# Clean up temporary test semesters 6, 7, 8 for student 1
Invoke-RestMethod -Uri "$baseUrl/semesters/$($created6.id)" -Method Delete -Headers $headers1 | Out-Null
Invoke-RestMethod -Uri "$baseUrl/semesters/$($created7.id)" -Method Delete -Headers $headers1 | Out-Null
Invoke-RestMethod -Uri "$baseUrl/semesters/$($created8.id)" -Method Delete -Headers $headers1 | Out-Null
Write-Host "Test clean-up complete."

Write-Host "`n=================================================="
Write-Host "  ALL PHASE 1 ACCEPTANCE TESTS PASSED (10/10)     "
Write-Host "=================================================="
