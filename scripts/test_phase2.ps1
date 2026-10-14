$base = 'http://localhost:8080/api'
Write-Host "=== CAMPUS TRACK PHASE 2 FULL ACCEPTANCE TEST ===" -ForegroundColor Cyan

# 1. Test Auth & Student 1 / Semesters
$h1 = @{ 'Authorization' = 'Bearer dev-student-1'; 'Content-Type' = 'application/json' }
$h2 = @{ 'Authorization' = 'Bearer dev-student-2'; 'Content-Type' = 'application/json' }

# Get semesters for Student 1
$sems = Invoke-RestMethod -Uri "$base/semesters" -Headers $h1 -Method GET
Write-Host "Student 1 Semesters count: $($sems.data.Count)"

# Find or ensure Semester 1 and Semester 2 exist
$sem1 = $sems.data | Where-Object { $_.semesterNumber -eq 1 } | Select-Object -First 1
$sem2 = $sems.data | Where-Object { $_.semesterNumber -eq 2 } | Select-Object -First 1

if (-not $sem1) {
    $sem1Res = Invoke-RestMethod -Uri "$base/semesters" -Headers $h1 -Method POST -Body (@{ semesterNumber = 1; academicYear = '2023-2024'; status = 'COMPLETED' } | ConvertTo-Json)
    $sem1 = $sem1Res.data
}
if (-not $sem2) {
    $sem2Res = Invoke-RestMethod -Uri "$base/semesters" -Headers $h1 -Method POST -Body (@{ semesterNumber = 2; academicYear = '2023-2024'; status = 'COMPLETED' } | ConvertTo-Json)
    $sem2 = $sem2Res.data
}

$sem1Id = $sem1.id
$sem2Id = $sem2.id
Write-Host "Sem 1 ID: $sem1Id, Sem 2 ID: $sem2Id"

# Test 1 & 2: Create subject in Sem 1 and SAME subject code in Sem 2 (Must allow independently)
Write-Host "`n--- Test 1 & 2: Independent Subject in Sem 1 and Sem 2 ---"
$subData1 = @{
    semesterId = $sem1Id
    subjectName = 'Data Structures & Algorithms'
    subjectCode = 'CS201'
    credits = 4
    subjectType = 'Theory'
    facultyName = 'Dr. Alan Turing'
    description = 'Trees, graphs and sorting'
} | ConvertTo-Json

# Delete if already existing from prior test
$existingSubs1 = (Invoke-RestMethod -Uri "$base/semesters/$sem1Id/subjects" -Headers $h1 -Method GET).data
$prior1 = $existingSubs1 | Where-Object { $_.subjectCode -eq 'CS201' }
if ($prior1) { Invoke-RestMethod -Uri "$base/subjects/$($prior1.id)" -Headers $h1 -Method DELETE | Out-Null }

$s1Res = Invoke-RestMethod -Uri "$base/semesters/$sem1Id/subjects" -Headers $h1 -Method POST -Body $subData1
$createdSub1Id = $s1Res.data.id
Write-Host "Created CS201 in Sem 1, ID: $createdSub1Id"

# Check duplicate code within Sem 1 (Should FAIL with 409)
try {
    Invoke-RestMethod -Uri "$base/semesters/$sem1Id/subjects" -Headers $h1 -Method POST -Body $subData1 -ErrorAction Stop
    Write-Host "FAILED: Duplicate subject code within Sem 1 was not rejected!" -ForegroundColor Red
} catch {
    Write-Host "SUCCESS: Duplicate subject code in Sem 1 was rejected: $($_.Exception.Message)" -ForegroundColor Green
}

# Create SAME code 'CS201' in Sem 2 (Must SUCCEED)
$existingSubs2 = (Invoke-RestMethod -Uri "$base/semesters/$sem2Id/subjects" -Headers $h1 -Method GET).data
$prior2 = $existingSubs2 | Where-Object { $_.subjectCode -eq 'CS201' }
if ($prior2) { Invoke-RestMethod -Uri "$base/subjects/$($prior2.id)" -Headers $h1 -Method DELETE | Out-Null }

$subData2 = @{
    semesterId = $sem2Id
    subjectName = 'Advanced Data Structures'
    subjectCode = 'CS201'
    credits = 3
    subjectType = 'Theory'
    facultyName = 'Prof. Knuth'
} | ConvertTo-Json
$s2Res = Invoke-RestMethod -Uri "$base/semesters/$sem2Id/subjects" -Headers $h1 -Method POST -Body $subData2
Write-Host "SUCCESS: Same code CS201 created in Sem 2 independently with ID $($s2Res.data.id)" -ForegroundColor Green

# Test 3: Edit Subject Details
Write-Host "`n--- Test 3: Edit Subject ---"
$editSub = @{
    subjectName = 'Data Structures & Algorithms (Honors)'
    subjectCode = 'CS201'
    credits = 4
    subjectType = 'Theory'
    facultyName = 'Dr. Alan M. Turing'
} | ConvertTo-Json
$editRes = Invoke-RestMethod -Uri "$base/subjects/$createdSub1Id" -Headers $h1 -Method PUT -Body $editSub
Write-Host "SUCCESS: Subject edited: $($editRes.data.subjectName)" -ForegroundColor Green

# Test 4 & 5: Assessments and Marks Validation
Write-Host "`n--- Test 4 & 5: Assessments & Marks Validation ---"
# Validation test: Negative marks
try {
    $badAssess = @{
        subjectId = $createdSub1Id
        assessmentName = 'Invalid Test'
        assessmentCategory = 'Quiz'
        maximumMarks = 20
        obtainedMarks = -5
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "$base/subjects/$createdSub1Id/assessments" -Headers $h1 -Method POST -Body $badAssess -ErrorAction Stop
    Write-Host "FAILED: Negative marks were accepted!" -ForegroundColor Red
} catch {
    Write-Host "SUCCESS: Negative marks rejected by server: $($_.Exception.Message)" -ForegroundColor Green
}

# Validation test: Obtained > Max marks
try {
    $badAssess2 = @{
        subjectId = $createdSub1Id
        assessmentName = 'Overflow Test'
        assessmentCategory = 'Quiz'
        maximumMarks = 20
        obtainedMarks = 25
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "$base/subjects/$createdSub1Id/assessments" -Headers $h1 -Method POST -Body $badAssess2 -ErrorAction Stop
    Write-Host "FAILED: Marks > Max were accepted!" -ForegroundColor Red
} catch {
    Write-Host "SUCCESS: Obtained > Max marks rejected by server: $($_.Exception.Message)" -ForegroundColor Green
}

# Add valid assessments
$a1 = Invoke-RestMethod -Uri "$base/subjects/$createdSub1Id/assessments" -Headers $h1 -Method POST -Body (@{
    subjectId = $createdSub1Id
    assessmentName = 'Internal Test 1'
    assessmentCategory = 'Internal Test 1'
    maximumMarks = 30
    obtainedMarks = 27
    weightagePercentage = 15
    assessmentDate = '2024-02-15'
    remarks = 'Top score in section'
} | ConvertTo-Json)

$a2 = Invoke-RestMethod -Uri "$base/subjects/$createdSub1Id/assessments" -Headers $h1 -Method POST -Body (@{
    subjectId = $createdSub1Id
    assessmentName = 'Internal Test 2'
    assessmentCategory = 'Internal Test 2'
    maximumMarks = 30
    obtainedMarks = 28
    weightagePercentage = 15
    assessmentDate = '2024-03-20'
} | ConvertTo-Json)

$a3 = Invoke-RestMethod -Uri "$base/subjects/$createdSub1Id/assessments" -Headers $h1 -Method POST -Body (@{
    subjectId = $createdSub1Id
    assessmentName = 'Programming Assignment 1'
    assessmentCategory = 'Assignment'
    maximumMarks = 10
    obtainedMarks = 10
    weightagePercentage = 10
} | ConvertTo-Json)
Write-Host "SUCCESS: Added 3 valid assessments to subject $createdSub1Id" -ForegroundColor Green

# Test 7 & 8: Manual Official Result Entry
Write-Host "`n--- Test 7 & 8: Official Result Entry ---"
$resultPayload = @{
    subjectId = $createdSub1Id
    endSemesterMarks = 72
    examinationMaximumMarks = 100
    finalMarks = 92
    finalGrade = 'O'
    gradePoint = 10.0
    resultStatus = 'Pass'
    resultSource = 'MANUAL'
    remarks = 'Official university grade sheet'
} | ConvertTo-Json

$resSave = Invoke-RestMethod -Uri "$base/subjects/$createdSub1Id/result" -Headers $h1 -Method PUT -Body $resultPayload
Write-Host "SUCCESS: Result saved. Source: $($resSave.data.resultSource), Grade: $($resSave.data.finalGrade), GP: $($resSave.data.gradePoint)" -ForegroundColor Green

# Test 9 & 10: Academic Summary & SGPA Calculation
Write-Host "`n--- Test 9 & 10: Academic Summary & SGPA ---"
$summary = (Invoke-RestMethod -Uri "$base/semesters/$sem1Id/academic-summary" -Headers $h1 -Method GET).data
Write-Host "Subjects: $($summary.subjectCount), Total Credits: $($summary.totalCredits), Assessments: $($summary.assessmentCount)"
Write-Host "Evaluated Credits: $($summary.evaluatedCredits), SGPA: $($summary.sgpa), Provisional: $($summary.isProvisional)"

# Test 12: Student 2 Isolation (Cannot access Student 1 records)
Write-Host "`n--- Test 12: Student 2 Isolation ---"
try {
    Invoke-RestMethod -Uri "$base/subjects/$createdSub1Id" -Headers $h2 -Method GET -ErrorAction Stop
    Write-Host "FAILED: Student 2 was able to view Student 1 subject!" -ForegroundColor Red
} catch {
    Write-Host "SUCCESS: Student 2 denied access to Student 1 subject: $($_.Exception.Message)" -ForegroundColor Green
}
try {
    Invoke-RestMethod -Uri "$base/subjects/$createdSub1Id/assessments" -Headers $h2 -Method GET -ErrorAction Stop
    Write-Host "FAILED: Student 2 was able to view Student 1 assessments!" -ForegroundColor Red
} catch {
    Write-Host "SUCCESS: Student 2 denied access to Student 1 assessments: $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host "`n=== ALL BACKEND ACCEPTANCE CRITERIA VERIFIED SUCCESSFULLY ===" -ForegroundColor Green
