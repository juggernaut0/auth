CREATE TABLE auth_user (
    id uuid UNIQUE PRIMARY KEY,
    email text UNIQUE NOT NULL,
    provider text
);

CREATE TABLE user_pass (
    id uuid UNIQUE PRIMARY KEY,
    auth_user_id uuid UNIQUE NOT NULL REFERENCES auth_user(id),
    hashed_pass text NOT NULL
);
CREATE INDEX user_pass_auth_user_id_idx ON user_pass(auth_user_id);

CREATE TABLE user_display_name (
    id uuid UNIQUE PRIMARY KEY,
    auth_user_id uuid NOT NULL REFERENCES auth_user(id),
    display_name text,
    effective_dt timestamp
);
CREATE INDEX user_display_name_auth_user_id_idx ON user_display_name(auth_user_id);
CREATE INDEX user_display_name_display_name_idx ON user_display_name(display_name);
