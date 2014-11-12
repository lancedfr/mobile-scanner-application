package za.ac.cput;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class Fragment2 extends Fragment {

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public Fragment2() {
    // Required empty public constructor
  }

  public static Fragment2 newInstance(String param1, String param2) {
    Fragment2 fragment = new Fragment2();
    Bundle args = new Bundle();

    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    getActivity().getActionBar().hide();

    View decorView = getActivity().getWindow().getDecorView();
    int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
    decorView.setSystemUiVisibility(uiOptions);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_fragment2, container, false);
    Button b = (Button) rootView.findViewById(R.id.button2);
    b.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent intent = new Intent(getActivity(), NewProductScannerActivity.class);
        startActivity(intent);

      }

    });
    return rootView;
  }

}
