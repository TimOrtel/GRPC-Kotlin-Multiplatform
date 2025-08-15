import SwiftUI
import Common

struct ContentView: View {
    @State private var hostName: String = ""
    @State private var port: String = ""
    @State private var greet: String = ""
    @State private var response: String = ""
    @State private var useHttps: Bool = false
    
    var isButtonEnabled: Bool {
        !hostName.isEmpty && Int(port) != nil && !greet.isEmpty
    }
    
    var body: some View {
        NavigationView {
            VStack(spacing: 16) {
                TextField("Server host name", text: $hostName)
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.never)
                    .textFieldStyle(RoundedBorderTextFieldStyle())

                TextField("Server port", text: $port)
                    .keyboardType(.numberPad)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    
                TextField("Enter your greeting", text: $greet)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    
                Toggle(isOn: $useHttps) {
                    Text("Use HTTPS")
                }
                
                Button(action: performGRPCRequest) {
                    Text("Click to perform GRPC request")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(isButtonEnabled ? Color.blue : Color.gray)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
                .disabled(!isButtonEnabled)
                
                Text("Response from server: \(response)")
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding()
            .navigationTitle("gRPC Example")
        }
    }
    
    private func performGRPCRequest() {
        guard let portNumber = Int32(port) else { return }
        Task {
            response = try await GreetingLogic().performGreeting(
                host: hostName,
                port: portNumber,
                useHttps: useHttps,
                message: greet
            )
        }
    }
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
