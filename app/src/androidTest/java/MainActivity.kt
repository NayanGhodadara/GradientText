import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.customtextview.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Dash animation
        findViewById<CustomTextView>(R.id.customTextView)
            .Animation()
            .setDashAnimation()
            .speed(1000)
            .setDirection(CustomTextView.Direction.FORWARD)
    }
}