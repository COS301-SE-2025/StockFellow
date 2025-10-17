import { useParams } from "react-router-dom";
import Header from "../components/layout/Header";
import Sidebar from "../components/layout/Sidebar";
import UserRequestForm from "../components/requests/UserRequestForm";

const RequestDetails = () => {
  const { id } = useParams();
//   const navigate = useNavigate();

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header />
        <main className="flex-1 overflow-x-hidden overflow-y-auto bg-gray-50">
          <div className="container mx-auto px-6 py-8">
            <UserRequestForm requestId={id} />
          </div>
        </main>
      </div>
    </div>
  );
};

export default RequestDetails;