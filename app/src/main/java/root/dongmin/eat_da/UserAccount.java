package root.dongmin.eat_da;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserAccount extends AppCompatActivity {


    public UserAccount(){

    }






    public String getIdToken()
    {return idToken;}

    public void setIdToken(String idToken)
    {
        this.idToken = idToken;
    }

    private String idToken;




    public String getEmailId()
    {
        return emailId;
    }
    public void setEmailId(String emailId)
    {
        this.emailId = emailId;
    }
    private String emailId;



    public String getPassword()
    {
        return password;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }
    private String password;









    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}