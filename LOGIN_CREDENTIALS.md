# WasteSmart Login Credentials

## Default Login Credentials for Testing

### 🏛️ Municipal/Admin Users

#### Super Admin
- **Email:** `admin@wastesmart.com`
- **Password:** `admin123`
- **Access:** Full system administration, all features

#### Municipal Admin
- **Email:** `municipal@wastesmart.com` 
- **Password:** `municipal123`
- **Access:** Municipal area management, waste reports, collector oversight

### 🚛 Waste Collector Users

#### Waste Collector
- **Email:** `collector@wastesmart.com`
- **Password:** `collector123`
- **Access:** Route management, collection tasks, status updates

#### Supervisor
- **Email:** `supervisor@wastesmart.com`
- **Password:** `supervisor123`
- **Access:** Team oversight, route planning, performance monitoring

### 👤 Regular Users
Regular citizens can register normally through the user registration flow.

## How to Use

### For Admin/Municipal Login:
1. From main screen, select "Admin Login" or navigate to AdminLoginActivity
2. Enter one of the admin credentials above
3. Click "Login"
4. You'll be taken to the Admin Dashboard

### For Collector Login:
1. From main screen, select "Collector Login" or navigate to CollectorLoginActivity  
2. Enter one of the collector credentials above
3. Click "Login"
4. You'll be taken to the Collector Dashboard

## Features by User Type

### Admin/Municipal Users Can:
- View all waste reports in the system
- Manage waste collector accounts
- Assign collection tasks
- View system analytics and reports
- Configure system settings
- Monitor collection performance

### Collector Users Can:
- View assigned collection routes
- Update collection status
- Mark waste reports as collected
- View route maps and navigation
- Update collection schedules
- Report issues or delays

### Regular Users Can:
- Report waste issues with photos and location
- Track their report status
- View collection schedules
- Update their profile information
- View waste collection history

## Security Notes

⚠️ **Important for Production:**
- These are default credentials for development/testing only
- In production, implement proper user management with secure passwords
- Consider implementing role-based access control (RBAC)
- Use Firebase Authentication for production user management
- Implement password reset functionality
- Add two-factor authentication for admin accounts

## Firebase Integration

The system also supports Firebase Authentication alongside these default credentials:
- Firebase users will be authenticated through Firebase Auth
- Default credentials bypass Firebase for quick testing
- Both authentication methods lead to the same dashboard functionality

## Troubleshooting

If login fails:
1. Check that credentials are typed exactly as shown (case-sensitive)
2. Ensure internet connection for Firebase authentication
3. Check Firebase configuration if using Firebase Auth
4. Verify that the target dashboard activities exist and are properly configured

## Adding New Users

To add new default users:
1. Add credentials to the respective Activity files
2. Update the `isDefaultAdminCredential()` or `isDefaultCollectorCredential()` methods
3. Update this documentation file
4. Consider implementing a proper user management system for production
