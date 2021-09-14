CREATE TABLE google_signin_details (
    id uuid UNIQUE PRIMARY KEY,
    auth_user_id uuid UNIQUE NOT NULL REFERENCES auth_user(id),
    google_id text NOT NULL
);
CREATE INDEX google_signin_details_google_id_idx ON google_signin_details(google_id);
