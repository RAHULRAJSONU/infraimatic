# Oil PSU customised modules – attribute reference

The Excel workbook `Oil PSU Customized Modules.xlsx` enumerates the data fields
required for several domain‑specific modules used by Oil Public Sector
undertakings.  This document summarises the attributes for each module and
explains nested structures where relevant.  Use these definitions when
creating templates in Infraimatic so that normalized submissions capture all
necessary data.

## Checklist

This module defines a reusable checklist.  A checklist may be associated with
daily or periodic inspections and includes a list of individual check points.

| Field | Description |
|------|-------------|
| **Id** | Unique identifier for the checklist. |
| **Name of Checklist** | Human‑readable name (e.g. “Daily safety checklist”). |
| **Company Id** | Identifier of the company or business unit this checklist belongs to. |
| **Check Points** | An array of check point definitions.  Each check point contains the following fields: `Sr No` (sequence number), `Check Point Name`, `Desired Value` (target value), `Mandatory` (boolean). |
| **Checklist Norm Reference** | Reference to governing standard or regulation (e.g. OISD guideline, factories act). |
| **Frequency** | How often the checklist should be executed (e.g. Daily, Weekly, Monthly, Quarterly). |
| **Created By** | Identifier of the user who created the checklist. |
| **Created on** | Timestamp when the checklist was created. |

## Daily TT Checking

This module records daily tank‑truck (TT) inspections and observations.  It
refers back to a checklist via `CheckList Id` and captures observed values.

| Field | Description |
|------|-------------|
| **Id** | Unique record identifier. |
| **Filling Advice Note (FAN) No** | The Filling Advice Note number associated with the truck. |
| **Filling Advice Note (FAN) Date** | Date of the FAN. |
| **CheckList Id** | Identifier of the checklist used for this inspection. |
| **Observations** | Array of observation entries.  Each entry includes: `Sr No` (sequence), `Observed values` (measured value), `Remarks`.  Remarks become mandatory if the observed value deviates from the desired value. |
| **Checked By** | Name or identifier of the person performing the check. |
| **Check Date/Time** | Timestamp when the check was completed. |
| **Allowed** | Whether the truck is allowed for operation (Yes/No). |

## TT Movement register

This module extends the movement register described earlier with additional
operational details.  It tracks crew details and entry/exit information for
each tank‑truck movement.

| Field | Description |
|------|-------------|
| **Id** | Unique movement record identifier. |
| **Vehicle No** | Identifier or license plate of the tank‑truck. |
| **TT Crew Details** | Array listing crew members.  Each element contains: `Sr No`, `Driver/Helper` (role), `Permanent/Temporary` (employment type), `Name`, `ID Card No`, `ID Card Validity` (expiry date), `Id Card Attachment` (document). |
| **Entry** | Details of the truck entry into the facility.  Fields include: `FAN No`, `FAN Date`, `Customer Code`, `Customer Name`, `Transporter code`, `Transporter Name`, `Entry Date/Time`, `Product Code`, `Product Name`, `Product Qty`, `Product Unit`. |
| **Exit** | Details of the truck leaving the facility.  Fields include: `Invoice No`, `Invoice Date`, `Customer Code`, `Customer Name`, `TT No` (truck number on invoice), `Exit Date/Time`, `Product Code`, `Product Name`, `Product Qty`, `Product Unit`. |
| **Amended** | Indicates if the record has been amended (Yes/No). |

## Quarterly TT Safety Check

Records safety inspections conducted quarterly on tank‑trucks.

| Field | Description |
|------|-------------|
| **Id** | Unique identifier of the safety check. |
| **Vehicle No** | Tank‑truck identifier. |
| **CheckList Id** | Checklist used for the safety check. |
| **Observations** | Array of observation entries.  Each observation includes: `Sr No`, `Observed Value`, `Remarks`, `Compliance Due Date`, `Compliance Date`, `Compliance Checked by`, `Compliance Remark`.  Remarks are mandatory if the observed value deviates from the desired value. |
| **Status** | Overall fitness status: `Fit`, `Partial Fit` or `Unfit`. |
| **Checked By** | Person who performed the safety check. |
| **Check Date/Time** | When the inspection was completed. |
| **Quarterly Fitness Valid upto** | Date until which this inspection remains valid. |

## TT Calibration Check

Captures calibration checks for metering equipment on the truck.

| Field | Description |
|------|-------------|
| **Id** | Unique calibration check record identifier. |
| **Vehicle No** | Tank‑truck identifier. |
| **CheckList Id** | Checklist used for calibration. |
| **Observations** | Array of observation entries with the same structure as the Quarterly Safety Check: `Sr No`, `Observed Value`, `Remarks`, `Compliance Due Date`, `Compliance Date`, `Compliance Checked by`, `Compliance Remark`.  Remarks are mandatory if the observed value deviates from the desired value. |
| **Checked By** | Person performing the calibration check. |
| **Check Date/Time** | Timestamp of the check. |
| **Remarks** | Optional free‑form notes (not mandatory). |

## Calibration Record

Keeps a log of calibration certificates issued to a truck.

| Field | Description |
|------|-------------|
| **Id** | Unique record identifier. |
| **Vehicle No** | Tank‑truck identifier. |
| **Calibration Certificate No** | Certificate number issued by the calibration facility. |
| **Calibration Certificate Date** | Date on which the certificate was issued. |
| **Calibration Certificate Validity** | Expiry date of the certificate. |
| **Calibration Facility Name** | Name of the facility that performed the calibration. |
| **Calibration Certificate Attachment** | Document containing the certificate (e.g. PDF). |
| **No of Leaf** | Number of compartments or leaves in the tank. |
| **Compartment Details** | Array capturing details of each compartment: `Sr No`, `Front/Back`, `Left/Right`, `No`.  Use this to record calibration values for each section of the tank. |

## Calibration Reverification

Used to record follow‑up checks after calibration.

| Field | Description |
|------|-------------|
| **Id** | Unique reverification record identifier. |
| **CheckList Id** | Identifier of the checklist used during reverification. |
| **Observations** | Array of observation entries.  Structure is the same as in the calibration and safety checks: `Sr No`, `Observed Value`, `Remarks`, `Compliance Due Date`, `Compliance Date`, `Compliance Checked by`, `Compliance Remark`.  Remarks are mandatory for deviations. |
| **Checked By** | Person performing the reverification. |
| **Check Date/Time** | When the reverification was performed. |
| **Remarks** | Optional free‑form notes. |

---

When creating templates for these modules in Infraimatic, represent nested
structures (e.g. `Check Points`, `Observations`, `Crew Details`) as arrays of
objects with the fields specified above.  You can use JSON Schema’s
`items` property to define the structure of each element and enforce
presence or optionality of specific fields.  Combining these definitions
with the earlier examples will allow you to capture comprehensive records
for each business process.
